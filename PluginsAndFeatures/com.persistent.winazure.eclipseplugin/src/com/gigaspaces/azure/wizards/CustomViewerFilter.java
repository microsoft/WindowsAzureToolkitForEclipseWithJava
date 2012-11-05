/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.gigaspaces.azure.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class CustomViewerFilter extends ViewerFilter {

	private final List<IProject> selProjects= new ArrayList<IProject>();
	// private final IProject selProject;
	private final String filter;

	/**
	 * f Create a new instance of a ViewPatternFilter
	 * 
	 * @param filter
	 * @param selProject
	 * @param isMatchItem
	 */
	public CustomViewerFilter(IProject selProject, String filter) {
		super();
		// this.selProject = selProject;
		this.filter = filter;
		this.selProjects.add(selProject);
	}

	/**
	 * Instantiates a new custom viewer filter.
	 *
	 * @param openProject the open project
	 * @param filter the filter
	 */
	public CustomViewerFilter(List<IProject> openProject, String filter) {
		super();
		this.filter = filter;
		// selProject=null;
		this.selProjects.addAll(openProject);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.dialogs.PatternFilter#isElementMatch(org.eclipse.jface
	 * .viewers.Viewer, java.lang.Object)
	 */
	protected boolean isLeafMatch(Viewer viewer, Object element) {
		if (element instanceof IProject) {

			return (((IProject) element).isOpen() && containsProject(element));
		}
		// if (element instanceof IProject) {
		// return ((IProject) element).isOpen()
		// && ((IProject) element == selProject);
		// }
		else if (element instanceof IFile) {
			boolean ext = ((IFile) element).getName().endsWith(filter);
			if (ext) {
				return true;
			}
		}

		return false;
	}

	private boolean containsProject(Object element) {
		for (IProject project : selProjects) {
			if (((IProject) element == project))
				return true;
		}

		return false;
	}

	private boolean notContainsProject(Object element) {
		for (IProject project : selProjects) {
			if (((IProject) element != project))
				return true;
		}

		return false;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return isElementVisible(viewer, element);
	}

	public boolean isElementVisible(Viewer viewer, Object element) {
		if (viewer == null)
			return true;
		return isParentMatch(viewer, element) || isLeafMatch(viewer, element);
	}

	protected boolean isParentMatch(Viewer viewer, Object element) {

		if (element instanceof IProject && notContainsProject(element))
			return false;

		// if (element instanceof IProject && ((IProject) element !=
		// selProject))
		// return false;

		Object[] children = ((ITreeContentProvider) ((AbstractTreeViewer) viewer)
				.getContentProvider()).getChildren(element);

		if ((children != null) && (children.length > 0)) {
			return isAnyVisible(viewer, element, children);
		}
		return false;
	}

	/**
	 * Returns true if any of the elements makes it through the filter. This
	 * method uses caching if enabled; the computation is done in
	 * computeAnyVisible.
	 * 
	 * @param viewer
	 * @param parent
	 * @param elements
	 *            the elements (must not be an empty array)
	 * @return true if any of the elements makes it through the filter.
	 */
	private boolean isAnyVisible(Viewer viewer, Object parent, Object[] elements) {

		boolean elementFound = false;
		for (int i = 0; i < elements.length && !elementFound; i++) {
			Object element = elements[i];
			elementFound = isElementVisible(viewer, element);
		}
		return elementFound;
	}
}
