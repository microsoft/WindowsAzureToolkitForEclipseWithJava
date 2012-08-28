package com.gigaspaces.uiautomation;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withMnemonic;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.results.WidgetResult;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.hamcrest.Matcher;

public class ContextMenuHelper {

	private static SWTBotMenuHolder menuHolder;
	public ContextMenuHelper(SWTBotMenuHolder holder)
	{
		menuHolder = holder;
	}
	public static class SWTBotMenuHolder{
		SWTBotMenu menu;
		public void setMenu(SWTBotMenu menu)
		{
			this.menu = menu;
		}
		public SWTBotMenu getMenu()
		{
			return menu;
		}
	}
	public SWTBotMenuHolder getMenuHolder()
	{
		return menuHolder;
	}
	/**
	 * Clicks the Windows Azure context menu and then clicks on the Publish menu.
	 */
	public ContextMenuHelper clickOnContextMenu(final AbstractSWTBot<?> bot, final String firstLevelMenuName, final String toClickMenuName) {
		// show
		final MenuItem menuItem = UIThreadRunnable
				.syncExec(new WidgetResult<MenuItem>() {
					@SuppressWarnings("unchecked")
					public MenuItem run() {
						MenuItem menuItem = null;
						Control control = (Control) bot.widget;
						Menu menu = control.getMenu();
						Matcher<?> matcher = allOf(instanceOf(MenuItem.class),withMnemonic(firstLevelMenuName));
						menuItem = show(menu, matcher);
						if (menuItem != null) {
							menu = menuItem.getMenu();
							SWTBotMenu swtBotMenu = new SWTBotMenu(menuItem).menu(toClickMenuName);
							menuHolder.setMenu(swtBotMenu);
							MenuItem returnMenu = new MenuItem(swtBotMenu.widget.getParent(), swtBotMenu.widget.getStyle());
							return returnMenu;
						}
						else 
							hide(menu);
						return null;
					}
				});
		if (menuItem == null) {
			throw new WidgetNotFoundException("Could not find menu: " + firstLevelMenuName + "->" + toClickMenuName);
		}

		// click
		click();
		// hide
		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				hide(menuItem.getParent());
			}
		});
		return this;
	}

	private static MenuItem show(final Menu menu, final Matcher<?> matcher) {
		if (menu != null) {
			menu.notifyListeners(SWT.Show, new Event());
			MenuItem[] items = menu.getItems();
			for (final MenuItem menuItem : items) {
				if (matcher.matches(menuItem)) {
					return menuItem;
				}
			}
			menu.notifyListeners(SWT.Hide, new Event());
		}
		return null;
	}
	
	private void click() {
		final Event event = new Event();
		event.time = (int) System.currentTimeMillis();
		event.widget = menuHolder.menu.widget;
		event.display = menuHolder.menu.display;
		event.type = SWT.Selection;

		UIThreadRunnable.asyncExec(menuHolder.menu.display, new VoidResult() {
			public void run() {
				menuHolder.menu.widget.notifyListeners(SWT.Selection, event);
			}
		});
	}

	private static void hide(final Menu menu) {
		menu.notifyListeners(SWT.Hide, new Event());
		if (menu.getParentMenu() != null) {
			hide(menu.getParentMenu());
		}
	}
	
	public static SWTBotMenu getSubMenuItem(final SWTBotMenu parentMenu, 
			final String itemText)
					throws WidgetNotFoundException {

		MenuItem menuItem = UIThreadRunnable.syncExec(new WidgetResult<MenuItem>() {
			public MenuItem run() {
				Menu bar = parentMenu.widget.getMenu();
				if (bar != null) {
					for (MenuItem item : bar.getItems()) {
						item.addDisposeListener(new DisposeListener() {

							public void widgetDisposed(DisposeEvent e) {
								System.out.println("Disposed Widget : " + e.widget);

							}
						});
						System.out.println(item);
						if (item.getText().equals(itemText)) {
							return item;
						}
					}
				}
				return null;
			}
		});

		if (menuItem == null) {
			throw new WidgetNotFoundException("MenuItem \"" + itemText + "\" not found.");
		} else {
			return new SWTBotMenu(menuItem);
		}
	}
}