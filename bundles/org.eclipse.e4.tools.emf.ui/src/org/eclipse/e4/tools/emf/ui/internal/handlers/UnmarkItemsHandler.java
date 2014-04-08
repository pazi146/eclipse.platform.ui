/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 432372
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.handlers;

import java.util.Collections;
import javax.inject.Named;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.IViewEObjects;
import org.eclipse.emf.ecore.EObject;

public class UnmarkItemsHandler extends AbstractHandler {

	@Execute
	public void execute(@Named(VIEWER_KEY) IViewEObjects viewer) {
		viewer.highlightEObjects(Collections.<EObject> emptyList());
	}

}