package com.raival.fileexplorer.tabs.file;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;
import com.raival.fileexplorer.App;
import com.raival.fileexplorer.R;
import com.raival.fileexplorer.activities.MainActivity;
import com.raival.fileexplorer.activities.TextEditorActivity;
import com.raival.fileexplorer.activities.model.MainViewModel;
import com.raival.fileexplorer.common.dialog.CustomDialog;
import com.raival.fileexplorer.common.view.BottomBarView;
import com.raival.fileexplorer.common.view.TabView;
import com.raival.fileexplorer.tabs.BaseTabFragment;
import com.raival.fileexplorer.tabs.checklist.ChecklistTabFragment;
import com.raival.fileexplorer.tabs.file.adapter.FileListAdapter;
import com.raival.fileexplorer.tabs.file.adapter.PathRootAdapter;
import com.raival.fileexplorer.tabs.file.dialog.SearchDialog;
import com.raival.fileexplorer.tabs.file.dialog.TasksDialog;
import com.raival.fileexplorer.tabs.file.holder.FileExplorerTabDataHolder;
import com.raival.fileexplorer.tabs.file.model.FileItem;
import com.raival.fileexplorer.tabs.file.option.FileOptionHandler;
import com.raival.fileexplorer.utils.FileUtil;
import com.raival.fileexplorer.utils.PrefsUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class FileExplorerTabFragment extends BaseTabFragment {
    public final static int MAX_NAME_LENGTH = 32;
    private final ArrayList<FileItem> files = new ArrayList<>();
    private RecyclerView fileList;
    private RecyclerView pathRootRv;
    private View placeHolder;
    private MainViewModel mainViewModel;
    private MaterialToolbar toolbar;
    private BottomBarView bottomBarView;
    private TabView.Tab tabView;
    private FileExplorerTabDataHolder dataHolder;
    private FileOptionHandler fileOptionHandler;
    private File previousDirectory;
    private File currentDirectory;

    public FileExplorerTabFragment() {
        super();
    }

    public FileExplorerTabFragment(File directory) {
        super();
        currentDirectory = directory;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.file_explorer_tab_fragment, container, false);
        fileList = view.findViewById(R.id.rv);
        pathRootRv = view.findViewById(R.id.path_root);
        placeHolder = view.findViewById(R.id.place_holder);

        view.findViewById(R.id.home).setOnClickListener(view1 -> setCurrentDirectory(getDefaultHomeDirectory()));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (toolbar == null) toolbar = ((MainActivity) requireActivity()).getToolbar();
        if (bottomBarView == null)
            bottomBarView = ((MainActivity) requireActivity()).getBottomBarView();

        if (mainViewModel == null) {
            mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        }

        prepareBottomBarView();
        initFileList();
        loadData();
        // restore RecyclerView state
        restoreRecyclerViewState();
    }

    public FileExplorerTabDataHolder getDataHolder() {
        if (dataHolder == null) {
            dataHolder = mainViewModel.getFileExplorerDataHolder(getTag());
        }
        if (dataHolder == null) {
            createNewDataHolder();
        }
        return dataHolder;
    }

    private void createNewDataHolder() {
        dataHolder = new FileExplorerTabDataHolder(getTag());
        dataHolder.activeDirectory = currentDirectory == null ? getDefaultHomeDirectory() : currentDirectory;
        mainViewModel.addDataHolder(dataHolder);
    }

    private void loadData() {
        setCurrentDirectory(getDataHolder().activeDirectory);
    }

    public void prepareBottomBarView() {
        bottomBarView.clear();
        bottomBarView.addItem("Tasks", R.drawable.ic_baseline_assignment_24, (view) ->
                new TasksDialog(this).show(getParentFragmentManager(), ""));

        bottomBarView.addItem("Search", R.drawable.ic_round_search_24, view -> {
            SearchDialog searchFragment = new SearchDialog(this, getCurrentDirectory());
            searchFragment.show(getParentFragmentManager(), "");
            setSelectAll(false);
        });
        bottomBarView.addItem("Create", R.drawable.ic_baseline_add_24, view -> showAddNewFileDialog());
        bottomBarView.addItem("Sort", R.drawable.ic_baseline_sort_24, this::showSortOptionsMenu);
        bottomBarView.addItem("Select All", R.drawable.ic_baseline_select_all_24, view -> setSelectAll(true));
        bottomBarView.addItem("refresh", R.drawable.ic_baseline_restart_alt_24, view -> refresh());
    }

    private void showSortOptionsMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(requireActivity(), view);

        popupMenu.getMenu().add("Sort by:").setEnabled(false);

        popupMenu.getMenu().add("Name (A-Z)").setCheckable(true).setChecked(PrefsUtil.getSortingMethod() == PrefsUtil.SORT_NAME_A2Z);
        popupMenu.getMenu().add("Name (Z-A)").setCheckable(true).setChecked(PrefsUtil.getSortingMethod() == PrefsUtil.SORT_NAME_Z2A);

        popupMenu.getMenu().add("Size (Bigger)").setCheckable(true).setChecked(PrefsUtil.getSortingMethod() == PrefsUtil.SORT_SIZE_BIGGER);
        popupMenu.getMenu().add("Size (Smaller)").setCheckable(true).setChecked(PrefsUtil.getSortingMethod() == PrefsUtil.SORT_SIZE_SMALLER);

        popupMenu.getMenu().add("Date (Newer)").setCheckable(true).setChecked(PrefsUtil.getSortingMethod() == PrefsUtil.SORT_DATE_NEWER);
        popupMenu.getMenu().add("Date (Older)").setCheckable(true).setChecked(PrefsUtil.getSortingMethod() == PrefsUtil.SORT_DATE_OLDER);

        popupMenu.getMenu().add("Other options:").setEnabled(false);

        popupMenu.getMenu().add("Folders first").setCheckable(true).setChecked(PrefsUtil.listFoldersFirst());

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            menuItem.setChecked(!menuItem.isChecked());
            switch (menuItem.getTitle().toString()) {
                case "Name (A-Z)": {
                    PrefsUtil.setSortingMethod(PrefsUtil.SORT_NAME_A2Z);
                    break;
                }
                case "Name (Z-A)": {
                    PrefsUtil.setSortingMethod(PrefsUtil.SORT_NAME_Z2A);
                    break;
                }
                case "Size (Bigger)": {
                    PrefsUtil.setSortingMethod(PrefsUtil.SORT_SIZE_BIGGER);
                    break;
                }
                case "Size (Smaller)": {
                    PrefsUtil.setSortingMethod(PrefsUtil.SORT_SIZE_SMALLER);
                    break;
                }
                case "Date (Older)": {
                    PrefsUtil.setSortingMethod(PrefsUtil.SORT_DATE_OLDER);
                    break;
                }
                case "Date (Newer)": {
                    PrefsUtil.setSortingMethod(PrefsUtil.SORT_DATE_NEWER);
                    break;
                }
                case "Folders first": {
                    PrefsUtil.setListFoldersFirst(menuItem.isChecked());
                    break;
                }
            }
            refresh();
            return true;
        });
        popupMenu.show();
    }

    private void showAddNewFileDialog() {
        TextInputLayout input = (TextInputLayout) getLayoutInflater().inflate(R.layout.input, null, false);
        input.setHint("File name");
        input.getEditText().setSingleLine();

        new CustomDialog()
                .setTitle("Create new file")
                .addView(input)
                .setPositiveButton("File", view ->
                        createFile(input.getEditText().getText().toString(), false), true)
                .setNegativeButton("Folder", view ->
                        createFile(input.getEditText().getText().toString(), true), true)
                .setNeutralButton("Cancel", null, true)
                .show(getParentFragmentManager(), "");
    }

    public void createFile(String name, boolean isFolder) {
        File file = new File(getCurrentDirectory(), name);
        if (isFolder) {
            if (!file.mkdir()) {
                App.showMsg("Unable to create folder: " + file.getAbsolutePath());
            } else {
                refresh();
                focusOn(file);
            }
        } else {
            try {
                if (!file.createNewFile()) {
                    App.showMsg("Unable to create file: " + file.getAbsolutePath());
                } else {
                    refresh();
                    focusOn(file);
                }
            } catch (IOException e) {
                App.showMsg(e.toString());
                App.log(e);
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        //  Unselect selected files (if any)
        if (getSelectedFiles().size() > 0) {
            setSelectAll(false);
            return true;
        }
        // Go back if possible
        final File parent = getCurrentDirectory().getParentFile();
        if (parent != null && parent.exists() && parent.canRead()) {
            setCurrentDirectory(getCurrentDirectory().getParentFile());
            // restore RecyclerView state
            restoreRecyclerViewState();
            return true;
        }
        // Close the tab (if not default tab)
        if (!getTag().startsWith("0_")) {
            // Remove the associated DataHolder
            mainViewModel.getDataHolders().removeIf(dataHolder1 -> dataHolder1.getTag().equals(getTag()));
            // Remove the tab
            ((MainActivity) requireActivity()).closeTab(getTag());
            return true;
        }

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    public void setSelectAll(boolean select) {
        for (FileItem item : files) {
            item.isSelected = select;
        }
        // Don't call refresh(), because it will recreate the tab and reset the selection
        fileList.getAdapter().notifyDataSetChanged();
    }

    public ArrayList<FileItem> getSelectedFiles() {
        final ArrayList<FileItem> list = new ArrayList<>();
        for (FileItem item : files) {
            if (item.isSelected) list.add(item);
        }
        return list;
    }

    /**
     * Show/Hide placeholder
     */
    public void showPlaceholder(boolean isShow) {
        placeHolder.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    /**
     * Used to update the title of attached tabView
     */
    private void updateTabTitle() {
        if (tabView == null) {
            if (!findAssociatedTabView()) {
                createNewTabView();
            }
        }
        tabView.setName(getName());
    }

    private void createNewTabView() {
        tabView = ((MainActivity) requireActivity()).getTabView().addNewTab(getTag());
    }

    private boolean findAssociatedTabView() {
        tabView = ((MainActivity) requireActivity()).getTabView().getTabByTag(getTag());
        return (tabView != null);
    }

    /**
     * This method is called once from #onViewCreated(View, Bundle)
     */
    private void initFileList() {
        fileList.setAdapter(new FileListAdapter(this));
        fileList.setHasFixedSize(true);
        initPathRoot();
    }

    private void initPathRoot() {
        pathRootRv.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));
        pathRootRv.setAdapter(new PathRootAdapter(this));
    }

    /**
     * RecyclerView state should be saved when the fragment is destroyed and recreated.
     * #getDataHolder() isn't used here because we don't want to create a new DataHolder if the fragment is about
     * to close (note that the DataHolder gets removed just right before the fragment is closed)
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dataHolder != null) {
            dataHolder.recyclerViewStates.put(getCurrentDirectory(), fileList.getLayoutManager().onSaveInstanceState());
        }
    }

    /**
     * This method automatically removes the restored state from DataHolder recyclerViewStates
     * This method is called when:
     * - Create the fragment
     * - #onBackPressed()
     * - when select a directory from pathRoot RecyclerView
     */
    public void restoreRecyclerViewState() {
        Parcelable savedState = getDataHolder().recyclerViewStates.get(getCurrentDirectory());
        if (savedState != null) {
            fileList.getLayoutManager().onRestoreInstanceState(savedState);
            getDataHolder().recyclerViewStates.remove(getCurrentDirectory());
        }
    }

    /**
     * Refreshes both fileList and pathRoot recyclerview (used by #setCurrentDirectory(File) ONLY)
     */
    private void refreshFileList() {
        fileList.getAdapter().notifyDataSetChanged();
        pathRootRv.getAdapter().notifyDataSetChanged();
        pathRootRv.scrollToPosition(pathRootRv.getAdapter().getItemCount() - 1);
        fileList.scrollToPosition(0);
        if (toolbar != null)
            toolbar.setSubtitle(FileUtil.getFormattedFileCount(getCurrentDirectory()));
    }

    /**
     * Used to refresh the tab
     */
    public void refresh() {
        setCurrentDirectory(getCurrentDirectory());
        restoreRecyclerViewState();
    }

    private File getDefaultHomeDirectory() {
        return Environment.getExternalStorageDirectory();
    }

    private void prepareSortedFiles() {
        // Make sure current file is ready
        if (getCurrentDirectory() == null) {
            loadData();
            return;
        }
        // Clear previous list
        files.clear();
        // Load all files in the current File
        File[] files = getCurrentDirectory().listFiles();
        if (files != null) {
            for (Comparator<File> comparator : FileUtil.getComparators()) {
                Arrays.sort(files, comparator);
            }
            for (File file : files) {
                this.files.add(new FileItem(file));
            }
        }
    }

    public void focusOn(File file) {
        for (int i = 0; i < files.size(); i++) {
            if (file.equals(files.get(i).file)) {
                fileList.scrollToPosition(i);
                return;
            }
        }
    }

    /**
     * @return the name associated with this tab (currently used for tabView)
     */
    public String getName() {
        return FileUtil.getShortLabel(getCurrentDirectory(), MAX_NAME_LENGTH);
    }

    public void showFileOptions(FileItem fileItem) {
        if (fileOptionHandler == null) {
            fileOptionHandler = new FileOptionHandler(this);
        }
        fileOptionHandler.showOptions(fileItem);
    }

    public void openFile(FileItem fileItem) {
        if (!handleKnownFileExtensions(fileItem)) {
            FileUtil.openFileWith(fileItem.file, false);
        }
    }

    private boolean handleKnownFileExtensions(FileItem fileItem) {
        if (FileUtil.isTextFile(fileItem.file) || FileUtil.isCodeFile(fileItem.file)) {
            Intent intent = new Intent();
            intent.setClass(requireActivity(), TextEditorActivity.class);
            intent.putExtra("file", fileItem.file.getAbsolutePath());
            requireActivity().startActivity(intent);
            return true;
        }
        if (FileUtil.getFileExtension(fileItem.file).equals("checklist")) {
            ((MainActivity) requireActivity()).addNewTab(new ChecklistTabFragment(fileItem.file)
                    , "ChecklistTabFragment_" + ((MainActivity) requireActivity()).generateRandomTag());
            return true;
        }
        return false;
    }

    public void showDialog(String title, String msg) {
        new CustomDialog()
                .setTitle(title)
                .setMsg(msg)
                .setPositiveButton("Ok", null, true)
                .showDialog(getParentFragmentManager(), "");
    }

    public ArrayList<FileItem> getFiles() {
        return files;
    }

    //______________| Getter and Setter |_______________\\

    public File getCurrentDirectory() {
        return currentDirectory;
    }

    /**
     * This method handles the following (in order):
     * - Updating currentDirectory and previousDirectory fields
     * - Updating recyclerViewStates in DataHolder
     * - Sorting files based on the preferences
     * - Updating tabView title
     * - Refreshing adapters (fileList & pathRoot)
     * - Updating activeDirectory in DataHolder
     *
     * @param dir the directory to open
     */
    public void setCurrentDirectory(File dir) {
        previousDirectory = currentDirectory;
        currentDirectory = dir;
        // Save only when previousDirectory is set (so that it can restore the state before onDestroy())
        if (previousDirectory != null) getDataHolder()
                .recyclerViewStates.put(previousDirectory, fileList.getLayoutManager().onSaveInstanceState());
        prepareSortedFiles();
        updateTabTitle();
        refreshFileList();
        getDataHolder().activeDirectory = getCurrentDirectory();
    }

    public File getPreviousDirectory() {
        return previousDirectory;
    }
}