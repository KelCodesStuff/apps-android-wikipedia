package org.wikipedia.readinglist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.wikipedia.R;
import org.wikipedia.activity.FragmentUtil;
import org.wikipedia.page.ExtendedBottomSheetDialogFragment;
import org.wikipedia.readinglist.database.ReadingList;
import org.wikipedia.readinglist.database.ReadingListDbHelper;
import org.wikipedia.readinglist.database.ReadingListPage;
import org.wikipedia.util.ResourceUtil;

import java.util.List;

public class ReadingListItemActionsDialog extends ExtendedBottomSheetDialogFragment {
    public interface Callback {
        void onToggleItemOffline(long pageId);
        void onShareItem(long pageId);
        void onAddItemToOther(long pageId);
        void onMoveItemToOther(long pageId);
        void onSelectItem(long pageId);
        void onDeleteItem(long pageId);
    }

    private static final String ARG_READING_LIST_NAME = "readingListName";
    private static final String ARG_READING_LIST_SIZE = "readingListSize";
    private static final String ARG_READING_LIST_PAGE = "readingListPage";
    private static final String ARG_READING_LIST_HAS_ACTION_MODE = "hasActionMode";

    private ReadingListPage readingListPage;
    private ReadingListItemActionsView actionsView;
    private ItemActionsCallback itemActionsCallback = new ItemActionsCallback();

    @NonNull
    public static ReadingListItemActionsDialog newInstance(@NonNull List<ReadingList> lists, long pageID, boolean hasActionMode) {
        ReadingListItemActionsDialog instance = new ReadingListItemActionsDialog();
        Bundle args = new Bundle();
        args.putString(ARG_READING_LIST_NAME, lists.get(0).title());
        args.putInt(ARG_READING_LIST_SIZE, lists.size());
        args.putLong(ARG_READING_LIST_PAGE, pageID);
        args.putBoolean(ARG_READING_LIST_HAS_ACTION_MODE, hasActionMode);
        instance.setArguments(args);
        return instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        actionsView = new ReadingListItemActionsView(getContext());
        actionsView.setBackgroundColor(ResourceUtil.getThemedColor(requireContext(), R.attr.paper_color));
        actionsView.setCallback(itemActionsCallback);
        readingListPage = ReadingListDbHelper.instance().getPageById(getArguments().getLong(ARG_READING_LIST_PAGE));
        if (readingListPage != null) {
            String removeFromListText = getArguments().getInt(ARG_READING_LIST_SIZE) == 1
                    ? getString(R.string.reading_list_remove_from_list, getArguments().getString(ARG_READING_LIST_NAME))
                    : getString(R.string.reading_list_remove_from_lists);
            actionsView.setState(readingListPage.title(), removeFromListText, readingListPage.offline(), getArguments().getBoolean(ARG_READING_LIST_HAS_ACTION_MODE));
        }
        return actionsView;
    }

    @Override
    public void onDestroyView() {
        actionsView.setCallback(null);
        actionsView = null;
        super.onDestroyView();
    }

    private class ItemActionsCallback implements ReadingListItemActionsView.Callback {
        @Override
        public void onToggleOffline() {
            dismiss();
            if (callback() != null) {
                callback().onToggleItemOffline(readingListPage.id());
            }
        }

        @Override
        public void onShare() {
            dismiss();
            if (callback() != null) {
                callback().onShareItem(readingListPage.id());
            }
        }

        @Override
        public void onAddToOther() {
            dismiss();
            if (callback() != null) {
                callback().onAddItemToOther(readingListPage.id());
            }
        }

        @Override
        public void onMoveToOther() {
            dismiss();
            if (callback() != null) {
                callback().onMoveItemToOther(readingListPage.id());
            }
        }

        @Override
        public void onSelect() {
            dismiss();
            if (callback() != null) {
                callback().onSelectItem(readingListPage.id());
            }
        }

        @Override
        public void onDelete() {
            dismiss();
            if (callback() != null) {
                callback().onDeleteItem(readingListPage.id());
            }
        }
    }

    @Nullable
    private Callback callback() {
        return FragmentUtil.getCallback(this, Callback.class);
    }
}
