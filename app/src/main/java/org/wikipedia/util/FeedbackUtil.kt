package org.wikipedia.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.TooltipCompat
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.skydoves.balloon.*
import org.wikipedia.R
import org.wikipedia.WikipediaApp
import org.wikipedia.activity.BaseActivity
import org.wikipedia.analytics.eventplatform.BreadCrumbLogEvent
import org.wikipedia.databinding.ViewPlainTextTooltipBinding
import org.wikipedia.dataclient.WikiSite
import org.wikipedia.main.MainActivity
import org.wikipedia.page.LinkMovementMethodExt
import org.wikipedia.page.PageActivity
import org.wikipedia.page.edithistory.EditHistoryListActivity
import org.wikipedia.random.RandomActivity
import org.wikipedia.readinglist.ReadingListActivity
import org.wikipedia.suggestededits.SuggestionsActivity
import org.wikipedia.talk.TalkTopicsActivity
import org.wikipedia.util.log.L
import org.wikipedia.views.AllowSnackbarOverBottomSheet

object FeedbackUtil {
    private const val LENGTH_SHORT = 3000
    private const val LENGTH_DEFAULT = 5000
    const val LENGTH_MEDIUM = 8000
    const val LENGTH_LONG = 15000
    private val TOOLBAR_ON_CLICK_LISTENER = View.OnClickListener { v ->
        showToastOverView(v, v.contentDescription, LENGTH_SHORT)
    }

    fun showError(activity: Activity, e: Throwable, wikiSite: WikiSite = WikipediaApp.instance.wikiSite) {
        val error = ThrowableUtil.getAppError(activity, e)
        makeSnackbar(activity, error.error, wikiSite = wikiSite).also {
            if (error.error.length > 200) {
                it.duration = Snackbar.LENGTH_INDEFINITE
                it.setAction(android.R.string.ok) { _ ->
                    it.dismiss()
                }
            }
            it.show()
        }
    }

    fun showMessageAsPlainText(activity: Activity, possibleHtml: CharSequence) {
        val richText: CharSequence = StringUtil.fromHtml(possibleHtml.toString())
        showMessage(activity, richText.toString())
    }

    fun showMessage(fragment: Fragment, @StringRes text: Int) {
        makeSnackbar(fragment.requireActivity(), fragment.getString(text), Snackbar.LENGTH_LONG).show()
    }

    fun showMessage(fragment: Fragment, text: String) {
        makeSnackbar(fragment.requireActivity(), text, Snackbar.LENGTH_LONG).show()
    }

    fun showMessage(activity: Activity, @StringRes resId: Int) {
        showMessage(activity, activity.getString(resId), Snackbar.LENGTH_LONG)
    }

    fun showMessage(activity: Activity, @StringRes resId: Int, duration: Int) {
        showMessage(activity, activity.getString(resId), duration)
    }

    fun showMessage(activity: Activity, text: CharSequence, duration: Int = Snackbar.LENGTH_LONG) {
        makeSnackbar(activity, text, duration).show()
    }

    fun showPrivacyPolicy(context: Context) {
        UriUtil.visitInExternalBrowser(context, Uri.parse(context.getString(R.string.privacy_policy_url)))
    }

    fun showTermsOfUse(context: Context) {
        UriUtil.visitInExternalBrowser(context, Uri.parse(context.getString(R.string.terms_of_use_url)))
    }

    fun showOfflineReadingAndData(context: Context) {
        UriUtil.visitInExternalBrowser(context, Uri.parse(context.getString(R.string.offline_reading_and_data_url)))
    }

    fun showAboutWikipedia(context: Context) {
        UriUtil.visitInExternalBrowser(context, Uri.parse(context.getString(R.string.about_wikipedia_url)))
    }

    fun showAndroidAppFAQ(context: Context) {
        UriUtil.visitInExternalBrowser(context, Uri.parse(context.getString(R.string.android_app_faq_url)))
    }

    fun showAndroidAppRequestAnAccount(context: Context) {
        UriUtil.visitInExternalBrowser(context, Uri.parse(context.getString(R.string.android_app_request_an_account_url)))
    }

    fun showAndroidAppEditingFAQ(context: Context,
                                 @StringRes urlStr: Int = R.string.android_app_edit_help_url) {
        UriUtil.visitInExternalBrowser(context, Uri.parse(context.getString(urlStr)))
    }

    fun composeEmail(context: Context,
                     emailAddress: String = context.getString(R.string.support_email),
                     subject: String = "",
                     body: String = "") {
        val intent = Intent()
            .setAction(Intent.ACTION_SENDTO)
            .setData(Uri.parse("mailto:$emailAddress?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}"))
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            L.e(e)
        }
    }

    fun setButtonTooltip(vararg views: View) {
        views.forEach { TooltipCompat.setTooltipText(it, it.contentDescription) }
    }

    fun setButtonOnClickToast(vararg views: View) {
        views.forEach { it.setOnClickListener(TOOLBAR_ON_CLICK_LISTENER) }
    }

    fun makeSnackbar(view: View, text: CharSequence, duration: Int = LENGTH_DEFAULT, wikiSite: WikiSite = WikipediaApp.instance.wikiSite): Snackbar {
        val snackbar = Snackbar.make(view, StringUtil.fromHtml(text.toString()).trim(), duration)
        val textView = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.setLinkTextColor(ResourceUtil.getThemedColor(view.context, R.attr.progressive_color))
        textView.movementMethod = LinkMovementMethodExt.getExternalLinkMovementMethod(wikiSite)
        return snackbar
    }

    fun makeSnackbar(activity: Activity, text: CharSequence, duration: Int = LENGTH_DEFAULT, wikiSite: WikiSite = WikipediaApp.instance.wikiSite): Snackbar {
        return makeSnackbar(findBestView(activity), text, duration, wikiSite)
    }

    fun showToastOverView(view: View, text: CharSequence?, duration: Int): Toast {
        val toast = Toast.makeText(view.context, text, duration)
        val v = LayoutInflater.from(view.context).inflate(androidx.appcompat.R.layout.abc_tooltip, null)
        val message = v.findViewById<TextView>(androidx.appcompat.R.id.message)
        message.text = StringUtil.removeHTMLTags(text.toString())
        message.maxLines = Int.MAX_VALUE
        toast.view = v
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        toast.setGravity(Gravity.TOP or Gravity.START, location[0], location[1])
        toast.show()
        return toast
    }

    fun showTooltip(activity: Activity, anchor: View, text: CharSequence, aboveOrBelow: Boolean,
                    autoDismiss: Boolean, arrowAnchorPadding: Int = 0, topOrBottomMargin: Int = 0, showDismissButton: Boolean = autoDismiss): Balloon {
        return showTooltip(activity, getTooltip(anchor.context, text, autoDismiss, arrowAnchorPadding, topOrBottomMargin, aboveOrBelow, showDismissButton), anchor, aboveOrBelow, autoDismiss)
    }

    fun showTooltip(activity: Activity, anchor: View, @LayoutRes layoutRes: Int,
                    arrowAnchorPadding: Int, topOrBottomMargin: Int, aboveOrBelow: Boolean, autoDismiss: Boolean): Balloon {
        return showTooltip(activity, getTooltip(anchor.context, layoutRes, arrowAnchorPadding, topOrBottomMargin, aboveOrBelow, autoDismiss), anchor, aboveOrBelow, autoDismiss)
    }

    private fun showTooltip(activity: Activity, balloon: Balloon, anchor: View, aboveOrBelow: Boolean, autoDismiss: Boolean): Balloon {
        if (aboveOrBelow) {
            balloon.showAlignTop(anchor, 0, DimenUtil.roundedDpToPx(8f))
        } else {
            balloon.showAlignBottom(anchor, 0, -DimenUtil.roundedDpToPx(8f))
        }
        if (!autoDismiss) {
            (activity as BaseActivity).setCurrentTooltip(balloon)
        }
        BreadCrumbLogEvent.logTooltipShown(activity, anchor)
        return balloon
    }

    fun getTooltip(context: Context, text: CharSequence, autoDismiss: Boolean, arrowAnchorPadding: Int = 0,
                   topOrBottomMargin: Int = 0, aboveOrBelow: Boolean = false, showDismissButton: Boolean = false,
                   @StringRes dismissButtonText: Int = R.string.onboarding_got_it, countNum: Int = 0, countTotal: Int = 0): Balloon {
        val binding = ViewPlainTextTooltipBinding.inflate(LayoutInflater.from(context))
        binding.textView.text = text
        binding.buttonView.isVisible = showDismissButton

        // Explicitly measure the width of the button text and set the button width, with some padding.
        // The Balloon library seems to present our custom layout in a way that causes the automatic
        // sizing of the button to be incorrect.
        val dismissText = context.getString(dismissButtonText)
        val bounds = Rect()
        binding.buttonView.paint.getTextBounds(dismissText, 0, dismissText.length, bounds)
        binding.buttonView.layoutParams = binding.buttonView.layoutParams.apply {
            width = bounds.width() + DimenUtil.roundedDpToPx(40f)
        }

        if (countTotal > 0) {
            binding.countView.isVisible = true
            binding.countView.text = context.getString(R.string.x_of_y, countNum, countTotal)
        }

        val balloon = createBalloon(context) {
            setArrowDrawableResource(R.drawable.ic_tooltip_arrow_up)
            setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            setArrowOrientationRules(ArrowOrientationRules.ALIGN_ANCHOR)
            setArrowSize(16)
            setMarginLeft(8)
            setMarginRight(8)
            setMarginTop(if (aboveOrBelow) 0 else topOrBottomMargin)
            setMarginBottom(if (aboveOrBelow) topOrBottomMargin else 0)
            setBackgroundColorResource(ResourceUtil.getThemedAttributeId(context, R.attr.progressive_color))
            setDismissWhenTouchOutside(autoDismiss)
            setLayout(binding.root)
            setWidth(BalloonSizeSpec.WRAP)
            setHeight(BalloonSizeSpec.WRAP)
            setArrowAlignAnchorPadding(arrowAnchorPadding)
        }

        binding.buttonView.setOnClickListener {
            balloon.dismiss()
        }

        return balloon
    }

    private fun getTooltip(context: Context, @LayoutRes layoutRes: Int, arrowAnchorPadding: Int,
                           topOrBottomMargin: Int, aboveOrBelow: Boolean, autoDismiss: Boolean): Balloon {
        return createBalloon(context) {
            setArrowDrawableResource(R.drawable.ic_tooltip_arrow_up)
            setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            setArrowOrientationRules(ArrowOrientationRules.ALIGN_ANCHOR)
            setArrowSize(24)
            setMarginLeft(8)
            setMarginRight(8)
            setMarginTop(if (aboveOrBelow) 0 else topOrBottomMargin)
            setMarginBottom(if (aboveOrBelow) topOrBottomMargin else 0)
            setBackgroundColorResource(ResourceUtil.getThemedAttributeId(context, R.attr.progressive_color))
            setDismissWhenTouchOutside(autoDismiss)
            setLayout(layoutRes)
            setWidth(BalloonSizeSpec.WRAP)
            setHeight(BalloonSizeSpec.WRAP)
            setArrowAlignAnchorPadding(arrowAnchorPadding)
        }
    }

    private fun findBestView(activity: Activity): View {
        // If the activity is currently displaying a bottom sheet over which we allow a snackbar,
        // use the bottom sheet's view as the anchor for the snackbar.
        getTopmostBottomSheetFragment(activity)?.let {
            return it.requireView()
        }

        // Otherwise, use the appropriate view for the activity.
        val viewId = when (activity) {
            is MainActivity -> R.id.fragment_main_coordinator
            is PageActivity -> R.id.fragment_page_coordinator
            is RandomActivity -> R.id.random_coordinator_layout
            is ReadingListActivity -> R.id.fragment_reading_list_coordinator
            is SuggestionsActivity -> R.id.suggestedEditsCardsCoordinator
            is EditHistoryListActivity -> R.id.edit_history_coordinator
            is TalkTopicsActivity -> R.id.talkTopicsSnackbar
            else -> android.R.id.content
        }
        return ActivityCompat.requireViewById(activity, viewId)
    }

    private fun getTopmostBottomSheetFragment(activity: Activity): BottomSheetDialogFragment? {
        (activity as? FragmentActivity)?.supportFragmentManager?.fragments?.forEach {
            getTopmostBottomSheetFragment(it)?.let { fragment ->
                return fragment
            }
        }
        return null
    }

    private fun getTopmostBottomSheetFragment(fragment: Fragment): BottomSheetDialogFragment? {
        if (fragment is BottomSheetDialogFragment && fragment is AllowSnackbarOverBottomSheet && fragment.view != null) {
            return fragment
        }
        fragment.childFragmentManager.fragments.forEach {
            if (getTopmostBottomSheetFragment(it) != null) {
                return it as BottomSheetDialogFragment
            }
        }
        return null
    }
}
