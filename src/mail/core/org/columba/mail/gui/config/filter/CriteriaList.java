// The contents of this file are subject to the Mozilla Public License Version
// 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.gui.config.filter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.columba.api.exception.PluginHandlerNotFoundException;
import org.columba.api.plugin.IExtension;
import org.columba.core.filter.Filter;
import org.columba.core.filter.FilterCriteria;
import org.columba.core.filter.FilterRule;
import org.columba.core.gui.dialog.NotifyDialog;
import org.columba.core.plugin.PluginManager;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.mail.gui.config.filter.plugins.DefaultCriteriaRow;
import org.columba.mail.plugin.FilterExtensionHandler;
import org.columba.mail.plugin.FilterUIExtensionHandler;

public class CriteriaList extends JPanel implements ActionListener,
		ItemListener {
	private Filter filter;

	private List list;

	private JPanel panel;

	private FilterExtensionHandler pluginHandler;

	private FilterUIExtensionHandler pluginUIHandler;

	public CriteriaList(Filter filter) {
		super();

		try {
			pluginHandler = (FilterExtensionHandler) PluginManager.getInstance()
					.getHandler(FilterExtensionHandler.NAME);
			pluginUIHandler = (FilterUIExtensionHandler) PluginManager
					.getInstance().getHandler(FilterUIExtensionHandler.NAME);
		} catch (PluginHandlerNotFoundException ex) {
			NotifyDialog d = new NotifyDialog();
			d.showDialog(ex);
		}

		this.filter = filter;

		list = new Vector();

		panel = new JPanel();

		JScrollPane scrollPane = new JScrollPane(panel);
		setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

		scrollPane.setPreferredSize(new Dimension(500, 100));
		setLayout(new BorderLayout());

		add(scrollPane, BorderLayout.CENTER);

		update();
	}

	public void updateComponents(boolean b) {
		if (!b) {
			for (Iterator it = list.iterator(); it.hasNext();) {
				DefaultCriteriaRow row = (DefaultCriteriaRow) it.next();

				row.updateComponents(false);
			}
		}
	}

	public void add() {
		FilterRule rule = filter.getFilterRule();
		rule.addEmptyCriteria();

		updateComponents(false);
		update();
	}

	public void remove(int i) {
		FilterRule rule = filter.getFilterRule();

		if (rule.count() > 1) {
			updateComponents(false);
			rule.remove(i);
			update();
		}
	}

	public void update() {
		panel.removeAll();
		list.clear();

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		panel.setLayout(gridbag);

		FilterRule rule = filter.getFilterRule();

		for (int i = 0; i < rule.count(); i++) {
			FilterCriteria criteria = rule.get(i);
			String type = criteria.getTypeString();
			DefaultCriteriaRow column = null;

			c.fill = GridBagConstraints.NONE;

			c.gridx = GridBagConstraints.RELATIVE;
			c.gridy = i;
			c.weightx = 1.0;
			c.anchor = GridBagConstraints.NORTHWEST;
			c.insets = new Insets(0, 0, 0, 0);
			c.gridwidth = 1;

			Object[] args = { pluginHandler, this, criteria };

			try {
				IExtension extension = pluginHandler.getExtension(type);
				String uiId = extension.getMetadata().getAttribute("ui");
				IExtension uiExtension = pluginUIHandler.getExtension(uiId);

				column = (DefaultCriteriaRow) uiExtension
						.instanciateExtension(args);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			// fall-back if error occurs
			if (column == null) {
				try {
					IExtension extension = pluginHandler.getExtension("Subject");
					String uiId = extension.getMetadata().getAttribute("ui");
					IExtension uiExtension = pluginUIHandler.getExtension(uiId);

					column = (DefaultCriteriaRow) uiExtension
							.instanciateExtension(args);
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				criteria.setTypeString("Subject");
			}

			gridbag.setConstraints(column.getContentPane(), c);
			list.add(column);

			panel.add(column.getContentPane());

			JButton addButton = new JButton(ImageLoader
					.getSmallImageIcon("stock_add_16.png"));
			addButton.setActionCommand("ADD");
			addButton.setMargin(new Insets(0, 0, 0, 0));
			addButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					add();
				}
			});

			JButton removeButton = new JButton(ImageLoader
					.getSmallImageIcon("stock_remove_16.png"));
			removeButton.setMargin(new Insets(0, 0, 0, 0));
			removeButton.setActionCommand(Integer.toString(i));

			final int index = i;
			removeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					remove(index);
				}
			});

			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridLayout(0, 2, 2, 2));
			buttonPanel.add(removeButton);
			buttonPanel.add(addButton);

			c.insets = new Insets(2, 2, 2, 2);
			c.gridx = GridBagConstraints.REMAINDER;
			c.anchor = GridBagConstraints.NORTHEAST;
			gridbag.setConstraints(buttonPanel, c);
			panel.add(buttonPanel);
		}

		c.weighty = 1.0;

		Component box = Box.createVerticalGlue();
		gridbag.setConstraints(box, c);
		panel.add(box);

		validate();
		repaint();
	}

	public void actionPerformed(ActionEvent e) {
		updateComponents(false);
		update();
	}

	/**
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent arg0) {
		updateComponents(false);
		update();

	}
}