package de.ecconia.logicworld.issuemanager.manager.window;

import de.ecconia.logicworld.issuemanager.manager.FilterManager;
import de.ecconia.logicworld.issuemanager.manager.Manager;
import de.ecconia.logicworld.issuemanager.manager.data.CategoryGroup;
import de.ecconia.logicworld.issuemanager.manager.data.FilterEntry;
import de.ecconia.logicworld.issuemanager.manager.data.FilterGroup;
import de.ecconia.logicworld.issuemanager.manager.window.helper.CButton;
import de.ecconia.logicworld.issuemanager.manager.window.helper.CDropDown;
import de.ecconia.logicworld.issuemanager.manager.window.helper.CPopup;
import de.ecconia.logicworld.issuemanager.manager.window.helper.CursorInteractableListener;
import de.ecconia.logicworld.issuemanager.manager.window.layout.FillWidthFlowLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class FilterBar extends JPanel
{
	private final Manager manager;
	private final FilterManager filterManager;
	
	private final JPanel configBar;
	
	public FilterBar(Manager manager, ManagerGUI window)
	{
		super(new BorderLayout());
		
		this.manager = manager;
		this.filterManager = manager.getFilterManager();
		
		// JPanel presetBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
		// presetBar.setBackground(Color.gray);
		// add(presetBar, BorderLayout.NORTH);
		configBar = new JPanel();
		configBar.setLayout(new FillWidthFlowLayout(configBar, 5));
		configBar.setBorder(new EmptyBorder(3, 4, 3, 4));
		configBar.setBackground(Color.gray);
		add(configBar);
		
		{
			// CDropDown<Filter> filterDropDown = new CDropDown<>(manager.getFilters().toArray(new Filter[0]));
			// filterDropDown.setPreferredSize(new Dimension(200, getFont().getSize() + 13));
			// presetBar.add(filterDropDown);
			
			// CButton save = new CButton("Save Filter");
			// save.setEnabled(false);
			// presetBar.add(save);
			// CButton saveAs = new CButton("Save Filter As");
			// presetBar.add(saveAs);
			// CButton delete = new CButton("Delete Filter");
			// delete.setEnabled(false);
			// presetBar.add(delete);
		}
		
		{
			CButton add = new CButton(" ＋ ");
			filterManager.addGroupChangeListener(() -> {
				boolean allDistributed = filterManager.getCurrentFilter().getGroupFilterAmount() == manager.getGroups().size();
				boolean shouldBeEnabled = !allDistributed;
				if(shouldBeEnabled != add.isEnabled())
				{
					add.setEnabled(shouldBeEnabled);
					add.repaint();
				}
			});
			add.addMouseListener(new CursorInteractableListener());
			add.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					//Add new setting to this thingy.
					CategoryGroup[] groups = manager.getGroups().toArray(new CategoryGroup[0]);
					CPopup popup = new CPopup();
					popup.setLayout(new GridLayout(0, 1));
					for(CategoryGroup group : groups)
					{
						if(filterManager.getCurrentFilter().hasFilterGroup(group))
						{
							continue;
						}
						JLabel text = new JLabel(group.getName());
						text.setOpaque(true);
						text.setBackground(Color.darkGray);
						text.setBorder(new LineBorder(Color.darkGray, 2));
						text.setForeground(Color.white);
						text.addMouseListener(new MouseAdapter()
						{
							@Override
							public void mouseReleased(MouseEvent e)
							{
								System.out.println("Clicked group " + group.getName());
								{
									//Do the adding:
									FilterGroup filterGroup = filterManager.getCurrentFilter().createFilterGroup(group);
									configBar.add(new FilterDropDown(filterGroup), configBar.getComponentCount() - 1);
									configBar.invalidate();
									configBar.revalidate();
									configBar.repaint();
								}
								popup.setVisible(false);
							}
							
							@Override
							public void mouseEntered(MouseEvent e)
							{
								text.setBorder(new LineBorder(new Color(0, 150, 0), 2));
								text.setBackground(new Color(0, 150, 0));
							}
							
							@Override
							public void mouseExited(MouseEvent e)
							{
								text.setBorder(new LineBorder(Color.darkGray, 2));
								text.setBackground(Color.darkGray);
							}
						});
						popup.add(text);
					}
					popup.show(add, 0, add.getHeight());
				}
			});
			configBar.add(add);
		}
	}
	
	private class FilterDropDown extends JPanel
	{
		private CPopup popup;
		
		public FilterDropDown(FilterGroup filterGroup)
		{
			super(new BorderLayout());
			setBackground(Color.darkGray);
			
			JPanel dropDownPanel = new JPanel();
			dropDownPanel.setBackground(Color.darkGray);
			dropDownPanel.setLayout(new VerySpecificFixLayout());
			dropDownPanel.setBorder(new EmptyBorder(4, 6, 4, 0)); //We still have some other component to the right, so no padding there.
			add(dropDownPanel);
			{
				JLabel label = new JLabel(filterGroup.getGroup().getName());
				label.setOpaque(true);
				label.setForeground(Color.white);
				label.setBackground(Color.darkGray);
				dropDownPanel.add(label);
				
				CDropDown.DropDownArrowComponent arrow = new CDropDown.DropDownArrowComponent(false);
				dropDownPanel.add(arrow);
			}
			
			CButton delButton = new CButton("X");
			delButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					configBar.remove(FilterDropDown.this);
					filterGroup.remove();
					configBar.invalidate();
					configBar.revalidate();
					configBar.repaint();
				}
			});
			add(delButton, BorderLayout.EAST);
			
			popup = new CPopup();
			popup.setLayout(new GridLayout(0, 1));
			for(FilterEntry entry : filterGroup.getFilterEntries())
			{
				popup.add(new FilterEntryElement(entry));
			}
			
			addMouseListener(new CursorInteractableListener());
			addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseClicked(MouseEvent e)
				{
					if(!popup.isVisible())
					{
						Insets insets = getInsets();
						Dimension size = popup.getMinimumSize();
						popup.setPreferredSize(new Dimension(Math.max(size.width, getWidth() - insets.left - insets.right), size.height));
						popup.invalidate();
						popup.revalidate();
						popup.show(FilterDropDown.this, insets.left, getHeight() - insets.bottom);
						popup.requestFocus();
					}
				}
			});
		}
		
		private static class VerySpecificFixLayout implements LayoutManager
		{
			@Override
			public void addLayoutComponent(String name, Component comp)
			{
			}
			
			@Override
			public void removeLayoutComponent(Component comp)
			{
			}
			
			@Override
			public Dimension preferredLayoutSize(Container parent)
			{
				return minimumLayoutSize(parent);
			}
			
			@Override
			public Dimension minimumLayoutSize(Container parent)
			{
				Insets insets = parent.getInsets();
				
				Component first = parent.getComponent(0);
				Dimension firstSize = first.getPreferredSize();
				
				return new Dimension(
						insets.left + firstSize.width + firstSize.height + insets.right,
						insets.top + firstSize.height + insets.bottom
				);
			}
			
			@Override
			public void layoutContainer(Container parent)
			{
				Insets insets = parent.getInsets();
				
				Component first = parent.getComponent(0);
				Dimension firstSize = first.getPreferredSize();
				Component second = parent.getComponent(1);
				
				first.setBounds(insets.left, insets.top, firstSize.width, firstSize.height);
				second.setBounds(insets.left + firstSize.width, insets.top, firstSize.height, firstSize.height);
			}
		}
	}
	
	private static class FilterEntryElement extends JPanel
	{
		private final FilterEntry filterEntry;
		
		public FilterEntryElement(FilterEntry filterEntry)
		{
			super(new BorderLayout());
			this.filterEntry = filterEntry;
			
			FilterChoiceBox choice = new FilterChoiceBox();
			add(choice, BorderLayout.EAST);
			JLabel label = new JLabel(filterEntry.getName());
			label.setOpaque(true);
			label.setBackground(Color.darkGray);
			label.setForeground(Color.white);
			add(label);
		}
		
		private class FilterChoiceBox extends JPanel
		{
			public FilterChoiceBox()
			{
				super(new GridLayout(1, 0));
				
				CButton no = new CButton("B");
				no.setForeground(Color.red);
				add(no);
				
				CButton idc = new CButton("/");
				idc.setActive(true);
				add(idc);
				
				CButton yes = new CButton("W");
				add(yes);
				yes.setForeground(Color.green);
				
				no.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseClicked(MouseEvent e)
					{
						no.setActive(true);
						idc.setActive(false);
						yes.setActive(false);
						filterEntry.setMode(FilterEntry.Mode.Blacklist);
					}
				});
				idc.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseClicked(MouseEvent e)
					{
						no.setActive(false);
						idc.setActive(true);
						yes.setActive(false);
						filterEntry.setMode(FilterEntry.Mode.Ignore);
					}
				});
				yes.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseClicked(MouseEvent e)
					{
						no.setActive(false);
						idc.setActive(false);
						yes.setActive(true);
						filterEntry.setMode(FilterEntry.Mode.Whitelist);
					}
				});
			}
		}
	}
}
