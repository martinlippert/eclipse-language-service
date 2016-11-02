/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Michał Niewrzał (Rogue Wave Software Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.languageserver.operations.symbols;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.languageserver.LanguageServiceAccessor;
import org.eclipse.languageserver.LanguageServiceAccessor.LSPDocumentInfo;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

import io.typefox.lsapi.DocumentSymbolParams;
import io.typefox.lsapi.ServerCapabilities;
import io.typefox.lsapi.SymbolInformation;
import io.typefox.lsapi.builders.DocumentSymbolParamsBuilder;

public class LSPSymbolInFileHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart part = HandlerUtil.getActiveEditor(event);
		if (part instanceof ITextEditor) {
			final ITextEditor textEditor = (ITextEditor) part;
			LSPDocumentInfo info = LanguageServiceAccessor.getLSPDocumentInfoFor(textEditor,
			        ServerCapabilities::isDocumentSymbolProvider);
			if (info == null) {
				return null;
			}
			final Shell shell = HandlerUtil.getActiveShell(event);
			DocumentSymbolParams params = new DocumentSymbolParamsBuilder().textDocument(info.getFileUri().toString())
			        .build();
			CompletableFuture<List<? extends SymbolInformation>> symbols = info.getLanguageClient()
			        .getTextDocumentService().documentSymbol(params);

			symbols.thenAccept((List<? extends SymbolInformation> t) -> {
				shell.getDisplay().asyncExec(() -> {
					LSPSymbolInFileDialog dialog = new LSPSymbolInFileDialog(shell, textEditor, t);
					dialog.open();
				});
			});
		}
		return null;
	}

}
