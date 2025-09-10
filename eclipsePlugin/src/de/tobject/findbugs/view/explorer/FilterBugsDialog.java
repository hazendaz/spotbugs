/*
 * Contributions to FindBugs
 * Copyright (C) 2009, Andrei Loskutov
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package de.tobject.findbugs.view.explorer;

import de.tobject.findbugs.FindbugsPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.internal.text.html.HTMLTextPresenter;
import org.eclipse.jface.text.DefaultInformationControl.IInformationPresenterExtension;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.dialogs.SelectionDialog;

import edu.umd.cs.findbugs.BugCode;
import edu.umd.cs.findbugs.BugPattern;
import edu.umd.cs.findbugs.DetectorFactory;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.Plugin;

/**
 * @author Andrei
 */
public class FilterBugsDialog extends SelectionDialog {

    private final class TreeSelectionChangedListener implements ISelectionChangedListener {
        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            var selection = (IStructuredSelection) event.getSelection();

            updateDescription(selection);
        }
    }

    private final class TreeCheckStateListener implements ICheckStateListener {
        @Override
        public void checkStateChanged(CheckStateChangedEvent event) {
            var element = event.getElement();
            var checked = event.getChecked();

            elementChecked(element, checked);
            updateTextIds();
        }
    }

    private final class TreeContentProvider implements ITreeContentProvider {
        @Override
        public Object[] getElements(Object inputElement) {
            return ((Collection<?>) inputElement).toArray();
        }

        @Override
        public Object[] getChildren(Object element) {
            if (element instanceof BugCode) {
                var children = getPatterns((BugCode) element);
                var array = children.toArray();
                Arrays.sort(array);
                return array;
            }
            return new Object[0];
        }

        @Override
        public Object getParent(Object element) {
            if (element instanceof BugPattern) {
                var pattern = (BugPattern) element;
                return DetectorFactoryCollection.instance().getBugCode(pattern.getAbbrev());
            }
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            return element instanceof BugCode;
        }

        @Override
        public void inputChanged(Viewer viewer1, Object oldInput, Object newInput) {
            // noop
        }

        @Override
        public void dispose() {
            // noop
        }
    }

    private static final class TreeLabelProvider implements ILabelProvider {
        @Override
        public Image getImage(Object element) {
            return null;
        }

        @Override
        public String getText(Object element) {
            if (element instanceof BugPattern) {
                var pattern = (BugPattern) element;
                return pattern.getType() + " (" + pattern.getCategory().toLowerCase() + ")";
            }
            if (element instanceof BugCode) {
                var code = (BugCode) element;
                return code.getAbbrev();// + " (" + code.getDescription() + ")";
            }
            return null;

        }

        @Override
        public void addListener(ILabelProviderListener listener) {
            // noop
        }

        @Override
        public void dispose() {
            // noop
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
            // noop
        }
    }

    class PatternFilteredTree extends FilteredTree {
        PatternFilteredTree(Composite parent, int treeStyle, PatternFilter filter) {
            super(parent, treeStyle, filter, true);
        }

        @Override
        protected TreeViewer doCreateTreeViewer(Composite parent1, int style) {
            checkList = createTree(parent1, style);
            return checkList;
        }

        @Override
        protected void clearText() {
            checkList.setCheckedElements(checkedElements);
            checkList.collapseAll();
            super.clearText();
        }

        public boolean isFiltering() {
            var filterString = getFilterString();
            return filterString != null && !filterString.isEmpty() && !filterString.equals(getInitialText());
        }
    }

    private final Set<BugPattern> allowedPatterns;

    private final Set<BugPattern> preSelectedPatterns;

    private final Set<BugCode> preSelectedTypes;

    private final Set<BugCode> allowedTypes;

    private final Map<BugCode, Set<BugPattern>> codeToPattern;

    private final Map<BugPattern, Set<DetectorFactory>> patternToFactory;

    private final Map<BugPattern, Set<Plugin>> patternToPlugin;

    private ContainerCheckedTreeViewer checkList;

    private TextPresentation presentation;

    private StyledText htmlControl;

    private IInformationPresenterExtension presenter;

    private Text selectedIds;

    /**
     * Contains logically consistent set of filtered elements. This set is NOT
     * the same as shown in the tree. The difference is: if parent is checked in
     * the tree, all the children are checked too. If child is checked in the
     * tree, the parent is checked too (grayed). However, we don't want to have
     * each child pattern if it's parent type is checked, and we don't want to
     * have parent type if only a subset of children is checked.
     */
    private Object[] checkedElements;

    private final TreeContentProvider contentProvider;

    private final TreeLabelProvider labelProvider;

    /**
     * Final result stored after dialog is closed
     */
    private String selectedAsText;

    public FilterBugsDialog(Shell parentShell, Set<BugPattern> filteredPatterns, Set<BugCode> filteredTypes) {
        super(parentShell);
        codeToPattern = new HashMap<>();
        patternToFactory = new HashMap<>();
        patternToPlugin = new HashMap<>();
        allowedPatterns = FindbugsPlugin.getKnownPatterns();
        allowedTypes = FindbugsPlugin.getKnownPatternTypes();
        preSelectedPatterns = filteredPatterns;
        preSelectedTypes = filteredTypes;
        contentProvider = new TreeContentProvider();
        labelProvider = new TreeLabelProvider();
        initMaps();

        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    private void initMaps() {
        for (BugPattern pattern : allowedPatterns) {
            var bugCode = DetectorFactoryCollection.instance().getBugCode(pattern.getAbbrev());
            getPatterns(bugCode).add(pattern);
        }
        // Filter out patterns if their types in the list
        // If at least one child is there, discard it from checked elements
        // list,
        // as it is already disabled by disabling parent
        var patterns = preSelectedPatterns.iterator();
        while (patterns.hasNext()) {
            var pattern = patterns.next();
            var bugCode = DetectorFactoryCollection.instance().getBugCode(pattern.getAbbrev());
            if (preSelectedTypes.contains(bugCode)) {
                patterns.remove();
            }
        }

        // merge types and the rest of patterns (without parent type)
        List<Object> merged = new ArrayList<>();
        merged.addAll(preSelectedTypes);
        merged.addAll(preSelectedPatterns);

        // for each type, ALL children should be preselected.
        for (BugCode bugCode : preSelectedTypes) {
            preSelectedPatterns.addAll(getPatterns(bugCode));
        }
        checkedElements = merged.toArray();
        sortCheckedElements();

        initDetectorMaps();
    }

    private void initDetectorMaps() {

        var iterator = DetectorFactoryCollection.instance().factoryIterator();
        while (iterator.hasNext()) {
            var factory = iterator.next();
            var patterns = factory.getReportedBugPatterns();
            for (BugPattern pattern : patterns) {
                Set<DetectorFactory> set = patternToFactory.computeIfAbsent(pattern,
                        k -> new TreeSet<>(Comparator.comparing(DetectorFactory::getFullName)));
                set.add(factory);

                Set<Plugin> pset = patternToPlugin.computeIfAbsent(pattern,
                        k -> new TreeSet<>(Comparator.comparing(Plugin::getPluginId)));
                pset.add(factory.getPlugin());
            }
        }
    }

    @Override
    public boolean close() {
        var text = selectedIds.getText();
        var computed = getSelectedIds();
        if (!text.isEmpty() && !computed.equals(text)) {
            // allow to specify filters using text area (no validation checks yet)
            // TODO validate text entered by user and throw away invalid/duplicated entries
            selectedAsText = text;
        } else {
            selectedAsText = computed;
        }
        return super.close();
    }

    public String getSelectedIds() {
        // in case dialog was closed, use well known result
        if (selectedAsText != null) {
            return selectedAsText;
        }
        var sb = new StringBuilder();
        for (Object object : checkedElements) {
            if (checkList.getGrayed(object)) {
                continue;
            }
            if (object instanceof BugCode) {
                var bugCode = (BugCode) object;
                sb.append(bugCode.getAbbrev()).append(", ");
            } else if (object instanceof BugPattern) {
                var pattern = (BugPattern) object;
                sb.append(pattern.getType()).append(", ");
            }
        }
        if (sb.length() > 2 && sb.indexOf(", ", sb.length() - 2) > 0) {
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }

    @Override
    protected Control createDialogArea(Composite parent) {

        final var sash = new SashForm(parent, SWT.HORIZONTAL);
        var layoutData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        layoutData.minimumHeight = 200;
        layoutData.minimumWidth = 200;
        layoutData.heightHint = 400;
        layoutData.widthHint = 500;
        layoutData.verticalIndent = 3;
        layoutData.horizontalIndent = 3;

        sash.setLayoutData(layoutData);

        var treeAndButtons = createGroup(sash, "Available pattern types and patterns");
        treeAndButtons.setLayout(new GridLayout());
        treeAndButtons.setLayoutData(new GridData(GridData.FILL_BOTH));

        final var tree = new PatternFilteredTree(treeAndButtons, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL
                | SWT.H_SCROLL | SWT.RESIZE, new PatternFilter());
        tree.setLayoutData(new GridData(GridData.FILL_BOTH));

        var buttons = new Composite(treeAndButtons, SWT.NONE);
        buttons.setLayout(new GridLayout(3, true));
        buttons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        final var button1 = new Button(buttons, SWT.PUSH);
        button1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        button1.setText("Select All");
        button1.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (false && tree.isFiltering()) {
                    toggleCheckedGroup(true);
                } else {
                    checkList.setAllChecked(true);
                    checkedElements = allowedTypes.toArray();
                }
                updateTextIds();
            }
        });

        final var button2 = new Button(buttons, SWT.PUSH);
        button2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        button2.setText("Deselect All");
        button2.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (false && tree.isFiltering()) {
                    toggleCheckedGroup(false);
                } else {
                    checkList.setAllChecked(false);
                    checkedElements = new Object[0];
                }
                updateTextIds();
            }
        });

        var rightPane = new SashForm(sash, SWT.VERTICAL);
        rightPane.setLayoutData(new GridData(GridData.FILL_BOTH));

        var group1 = createGroup(rightPane, "Description");
        htmlControl = new StyledText(group1, SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.WRAP);
        presentation = new TextPresentation();
        htmlControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        presenter = new HTMLTextPresenter(false);

        var group2 = createGroup(rightPane, "Filtered pattern types and patterns");
        selectedIds = new Text(group2, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.WRAP);

        selectedIds.setLayoutData(new GridData(GridData.FILL_BOTH));

        updateTextIds();

        return sash;
    }

    private void toggleCheckedGroup(boolean on) {

        if (on) {
            // TODO currently it checks for all existing, but it should check
            // only visible
            var elements = checkList.getVisibleExpandedElements();
            List<Object> list = Arrays.asList(checkedElements);
            for (Object object : elements) {
                if (!list.contains(object)) {
                    elementChecked(object, on);
                }
            }
        } else {
            // TODO currently it checks for all existing, but it should check only visible
            var elements = checkList.getVisibleExpandedElements();
            List<Object> list = Arrays.asList(checkedElements);
            for (Object object : elements) {
                var parent = contentProvider.getParent(object);
                if (list.contains(object) || list.contains(parent)) {
                    elementChecked(object, on);
                }
            }
        }
        sortCheckedElements();
        checkList.setCheckedElements(checkedElements);
    }

    private Group createGroup(Composite composite, String name) {
        var group = new Group(composite, SWT.NONE);
        group.setLayout(new GridLayout());
        var data = new GridData(GridData.FILL_BOTH);
        // data.verticalIndent = -20;
        // data.horizontalIndent = -20;
        group.setLayoutData(data);
        group.setText(name);
        return group;
    }

    private ContainerCheckedTreeViewer createTree(Composite parent, int style) {
        final ContainerCheckedTreeViewer viewer = new ContainerCheckedTreeViewer(parent, style | SWT.SINGLE | SWT.BORDER
                | SWT.V_SCROLL | SWT.H_SCROLL | SWT.RESIZE) {
            /**
             * Overridden to re-set checked state of elements after filter change
             */
            @Override
            public void refresh(boolean updateLabels) {
                super.refresh(updateLabels);
                setCheckedElements(checkedElements);
            }
        };

        viewer.setContentProvider(contentProvider);
        viewer.setLabelProvider(labelProvider);
        viewer.setInput(allowedTypes);
        var preselected = getPreselected();
        viewer.setCheckedElements(preselected);
        viewer.addPostSelectionChangedListener(new TreeSelectionChangedListener());
        viewer.getTree().addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                updateDescription((IStructuredSelection) viewer.getSelection());
            }
        });
        viewer.addCheckStateListener(new TreeCheckStateListener());
        return viewer;
    }

    private Object[] getPreselected() {
        List<Object> all = new ArrayList<>(preSelectedPatterns);
        all.addAll(preSelectedTypes);
        return all.toArray();
    }

    @Override
    protected IDialogSettings getDialogBoundsSettings() {
        var dialogSettings = FindbugsPlugin.getDefault().getDialogSettings();
        var section = dialogSettings.getSection("FilterBugDialog");
        if (section == null) {
            dialogSettings.addNewSection("FilterBugDialog");
        }
        return section;
    }

    private Set<BugPattern> getPatterns(BugCode bugCode) {
        var set = codeToPattern.get(bugCode);
        if (set != null) {
            return set;
        }
        set = new HashSet<>();
        codeToPattern.put(bugCode, set);
        return set;
    }

    private void updateTextIds() {
        selectedIds.setText(getSelectedIds());

        var selTypes = checkedElements.length;
        for (Object object : checkedElements) {
            if (object instanceof BugPattern) {
                selTypes--;
            }
        }
        selectedIds.setToolTipText("Available types: " + allowedTypes.size() + ", available patterns: " + allowedPatterns.size()
        + ", selected types: " + selTypes + ", patterns: " + (checkedElements.length - selTypes));
    }

    private void toggleElement(boolean on, Object element, Set<Object> set) {
        if (on) {
            set.add(element);
        } else {
            set.remove(element);
        }
    }

    protected void elementChecked(Object element, boolean checked) {
        Set<Object> selected = new HashSet<>(Arrays.asList(checkedElements));
        toggleElement(checked, element, selected);
        if (element instanceof BugCode) {
            var children = getPatterns((BugCode) element);
            // just remove children, because we have parent
            selected.removeAll(children);
        } else {

            var parentEl = contentProvider.getParent(element);
            if (parentEl instanceof BugCode) {
                var children = getPatterns((BugCode) parentEl);
                var all = true;
                for (Object object : children) {
                    if (object == element) {
                        continue;
                    }
                    if (checked != checkList.getChecked(object)) {
                        all = false;
                        break;
                    }
                }
                if (all) {
                    toggleElement(checked, parentEl, selected);
                    selected.removeAll(children);
                } else if (checkList.getChecked(parentEl)) {
                    toggleElement(false, parentEl, selected);
                    for (Object object : children) {
                        toggleElement(checkList.getChecked(object), object, selected);
                    }
                }
            }
        }

        checkedElements = selected.toArray();
        sortCheckedElements();
    }

    private void sortCheckedElements() {
        Arrays.sort(checkedElements, (o1, o2) -> {
            var text1 = labelProvider.getText(o1);
            var text2 = labelProvider.getText(o2);
            if (text1 == null) {
                return -1;
            }
            if (text2 == null) {
                return 1;
            }
            return text1.compareTo(text2);
        });
    }

    private void updateDescription(IStructuredSelection selection) {
        var element = selection.getFirstElement();
        // HTMLTextPresenter uses LineBreakingReader/BufferedReader in Eclipse 4.4 which expects non-empty strings (while parsing html line breaks).
        var txt = " ";
        if (element instanceof BugPattern) {
            var pattern = (BugPattern) element;
            txt = getPatternDescription(pattern);
        } else if (element instanceof BugCode) {
            var code = (BugCode) element;
            txt = getPatternTypeDescription(code);
        }
        var size = htmlControl.getClientArea();
        if (size.width > 0 && size.height > 0) {
            txt = presenter.updatePresentation(getShell().getDisplay(), txt, presentation, size.width, size.height);
        }
        htmlControl.setText(txt);
    }

    private String getPatternDescription(BugPattern pattern) {
        var sb = new StringBuilder(pattern.getDetailText());
        var plugins = patternToPlugin.get(pattern);
        for (Plugin plugin : plugins) {
            sb.append("<p>");
            appendPluginDescription(sb, plugin);
        }
        return sb.toString();
    }

    private void appendPluginDescription(StringBuilder sb, Plugin plugin) {
        sb.append("<p>Contributed by plugin: ").append(plugin.getPluginId());
        sb.append("<p>Provider: ").append(plugin.getProvider());
        var website = plugin.getWebsite();
        if (website != null && !website.isEmpty()) {
            sb.append(" (").append(website).append(")");
        }
    }

    private String getPatternTypeDescription(BugCode code) {
        var sb = new StringBuilder(code.getDescription());
        sb.append("<p><br>Patterns:<br>");
        var patterns = getPatterns(code);
        for (BugPattern bugPattern : patterns) {
            sb.append(bugPattern.getType()).append("<br>");
        }
        // add reported by...
        Set<DetectorFactory> allFactories = new TreeSet<>(Comparator.comparing(DetectorFactory::getFullName));
        for (BugPattern bugPattern : patterns) {
            var set = patternToFactory.get(bugPattern);
            if (set != null) {
                allFactories.addAll(set);
            } else if (shouldReportMissing(bugPattern)) {
                FindbugsPlugin.getDefault().logError(
                        "Pattern not reported by any detector, but defined in findbugs.xml: " + bugPattern);
            }
        }
        sb.append("<p>Reported by:<br>");
        for (DetectorFactory factory : allFactories) {
            sb.append(factory.getFullName());
            appendPluginDescription(sb, factory.getPlugin());
            sb.append("<p><p>");
        }
        return sb.toString();
    }

    private boolean shouldReportMissing(BugPattern bugPattern) {
        return !bugPattern.isDeprecated()
                // reported many times by some test code
                && !"UNKNOWN".equals(bugPattern.getType())
                // reported many times by some test code
                && !"EXPERIMENTAL".equals(bugPattern.getCategory())
                // reported by FindBugs2 itself (no one detector factory
                // exists):
                && !"SKIPPED_CLASS_TOO_BIG".equals(bugPattern.getType());
    }
}
