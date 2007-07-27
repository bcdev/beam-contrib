package org.esa.beam.decisiontree.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.esa.beam.framework.ui.TableLayout;

import com.bc.ceres.binding.ValueContainer;
import com.bc.ceres.binding.swing.SwingBindingContext;

class OfewClassificationPanel extends JPanel {

    private JTextField[] variablesTextFields;
	private ValueContainer[] valueContainers;
	private JComboBox roiCombo;
	private JCheckBox roiCheckBox;
    

	public OfewClassificationPanel(OfewClassificationPresenter presenter) {
		valueContainers = presenter.getVariableValueContainers();
		variablesTextFields = new JTextField[valueContainers.length];
		
		initComponents(presenter);
		bindComponents();
    }
	
	public void postActionEvent() {
		for (JTextField textField : variablesTextFields) {
			textField.postActionEvent();
		}
	}
	
	public String getRoiBandName() {
		if (roiCheckBox.isSelected()) {
			String name = (String) roiCombo.getSelectedItem();
			return name;
		}
		return "";
	}

    private void bindComponents() {
    	
    	for (int i = 0; i < valueContainers.length; i++) {
    		ValueContainer container = valueContainers[i];
    		SwingBindingContext bindingContext = new SwingBindingContext(container);
    		bindingContext.bind(variablesTextFields[i], "value");
		}
    }

    private void initComponents(OfewClassificationPresenter presenter) {
        TableLayout tableLayout = new TableLayout(1);
		setLayout(tableLayout);
        tableLayout.setTableAnchor(TableLayout.Anchor.LINE_START);
        tableLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
        tableLayout.setColumnWeightX(0, 1);
        tableLayout.setTablePadding(2, 2);
        
        TableLayout inputTableLayout = new TableLayout(2);
        inputTableLayout.setTableAnchor(TableLayout.Anchor.LINE_START);
        inputTableLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
        inputTableLayout.setColumnWeightX(0, 0.1);
        inputTableLayout.setColumnWeightX(1, 1);
//        inputTableLayout.setCellColspan(1, 0, 2);
        inputTableLayout.setTablePadding(2, 2);
		JPanel inputPanel = new JPanel(inputTableLayout);

        inputPanel.setBorder(BorderFactory.createTitledBorder(null, "Eingabe",
                                                                TitledBorder.DEFAULT_JUSTIFICATION,
                                                                TitledBorder.DEFAULT_POSITION,
                                                                new Font("Tahoma", 0, 11),
                                                                new Color(0, 70, 213)));
        inputPanel.add(new JLabel("Eingabe-Produkt:"));
        JTextField inputProductTextField = new JTextField(presenter.getInputProduct().getName());
        inputProductTextField.setEditable(false);
		inputPanel.add(inputProductTextField);
		
		String[] bandsWithRoi = presenter.getBandsWithRoi();
		if (bandsWithRoi.length > 0) {
			roiCheckBox = new JCheckBox("Nur in der ROI von ", true);
			inputPanel.add(roiCheckBox);
			roiCombo = new JComboBox(bandsWithRoi);
			inputPanel.add(roiCombo);
			
			roiCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					roiCombo.setEnabled(roiCheckBox.isSelected());
				}
			});
		}
		add(inputPanel);
		
        if (valueContainers.length != 0) {
        	TableLayout paramTableLayout = new TableLayout(2);
        	paramTableLayout.setTableAnchor(TableLayout.Anchor.LINE_START);
        	paramTableLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
        	paramTableLayout.setColumnWeightX(0, 0.1);
        	paramTableLayout.setColumnWeightX(1, 1);
        	paramTableLayout.setTablePadding(2, 2);
        	JPanel paramPanel = new JPanel(paramTableLayout);

        	paramPanel.setBorder(BorderFactory.createTitledBorder(null, "Einstellungen",
                                                                TitledBorder.DEFAULT_JUSTIFICATION,
                                                                TitledBorder.DEFAULT_POSITION,
                                                                new Font("Tahoma", 0, 11),
                                                                new Color(0, 70, 213)));
        	for (int i = 0; i < valueContainers.length; i++) {
        		paramPanel.add(new JLabel((String) valueContainers[i].getValue("description") + ":"));
        		
        		JTextField textField = new JTextField(4);
        		textField.setHorizontalAlignment(JTextField.RIGHT);
        		paramPanel.add(textField);
        		variablesTextFields[i] = textField;
			}
		
        	add(paramPanel);
        }
        
        
        TableLayout outputTableLayout = new TableLayout(2);
        outputTableLayout.setTableAnchor(TableLayout.Anchor.LINE_START);
        outputTableLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
        outputTableLayout.setColumnWeightX(0, 0.1);
        outputTableLayout.setColumnWeightX(1, 1);
        outputTableLayout.setTablePadding(2, 2);
		JPanel outputPanel = new JPanel(outputTableLayout);

		outputPanel.setBorder(BorderFactory.createTitledBorder(null, "Ausgabe",
                                                                TitledBorder.DEFAULT_JUSTIFICATION,
                                                                TitledBorder.DEFAULT_POSITION,
                                                                new Font("Tahoma", 0, 11),
                                                                new Color(0, 70, 213)));
		outputPanel.add(new JLabel("Klassifikations-Produkt:"));
		outputPanel.add(new JTextField(16));
		outputPanel.add(new JLabel("Entmischungs-Produkt:"));
		outputPanel.add(new JTextField(16));
		outputPanel.add(new JLabel("Index-Produkt:"));
		outputPanel.add(new JTextField(16));
        add(outputPanel);
    }
}
