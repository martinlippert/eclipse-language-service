/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.languageserver.ui;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.languageserver.LSPStreamConnectionProviderRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class LanguageServerPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private LSPStreamConnectionProviderRegistry registry;
	private LinkedHashMap<IContentType, ILaunchConfiguration> workingCopy;
	private Button removeButton;
	private TableViewer viewer;

	public LanguageServerPreferencePage() {
	}

	public LanguageServerPreferencePage(String title) {
		super(title);
	}

	public LanguageServerPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	public void init(IWorkbench workbench) {
		this.registry = LSPStreamConnectionProviderRegistry.getInstance();
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite res = new Composite(parent, SWT.NONE);
		res.setLayout(new GridLayout(2, false));
		Label intro = new Label(res, SWT.WRAP);
		intro.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
		//intro.setText(Messages.PreferencesPage_Intro);
		viewer = new TableViewer(res);
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.setContentProvider(new ArrayContentProvider());
		workingCopy = new LinkedHashMap<>();
		workingCopy.putAll(LSPStreamConnectionProviderRegistry.getInstance().getContentTypeToLSPLaunches());
		TableViewerColumn contentTypeColumn = new TableViewerColumn(viewer, SWT.NONE);
		contentTypeColumn.getColumn().setText(Messages.PreferencesPage_contentType);
		contentTypeColumn.getColumn().setWidth(200);
		contentTypeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Entry<IContentType, ILaunchConfiguration>)element).getKey().getName();
			}
		});
		TableViewerColumn launchConfigColumn = new TableViewerColumn(viewer, SWT.NONE);
		launchConfigColumn.getColumn().setText(Messages.PreferencesPage_LaunchConfiguration);
		launchConfigColumn.getColumn().setWidth(300);
		launchConfigColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Entry<IContentType, ILaunchConfiguration>)element).getValue().getName();
			}
		});
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.getTable().setHeaderVisible(true);
		Composite buttonComposite = new Composite(res, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(1, false));
		Button addButton = new Button(buttonComposite, SWT.PUSH);
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		addButton.setText(Messages.PreferencesPage_Add);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				NewContentTypeLSPLaunchDialog dialog = new NewContentTypeLSPLaunchDialog(getShell());
				if (dialog.open() == IDialogConstants.OK_ID) {
					workingCopy.put(dialog.getContentType(), dialog.getLaunchConfiguration());
					viewer.refresh();
				}
				super.widgetSelected(e);
			}
		});
		removeButton = new Button(buttonComposite, SWT.PUSH);
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		removeButton.setText(Messages.PreferencesPage_Remove);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection sel = viewer.getSelection();
				if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
					for (Object item : ((IStructuredSelection)sel).toArray()) {
						workingCopy.remove(((Entry<IContentType, ILaunchConfiguration>)item).getKey());
					}
					viewer.refresh();
				}
			}
		});
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
			}
		});
		viewer.setInput(workingCopy.entrySet());
		updateButtons();
		return res;
	}
	
	protected void updateButtons() {
		this.removeButton.setEnabled(!this.viewer.getSelection().isEmpty());
	}

	@Override
	public boolean performOk() {
		this.registry.setAssociations(this.workingCopy);
		return super.performOk();
	}

}