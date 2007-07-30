package org.esa.beam.ofew.ui;

import com.bc.ceres.binding.swing.SwingBindingContext;
import org.esa.beam.framework.ui.TableLayout;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Ralf Quast
 * @version $Revision$ $Date$
 */
class AtmCorrPanel extends JPanel {

    AtmCorrPresenter presenter;

    JFormattedTextField[] textFieldsA;
    JFormattedTextField[] textFieldsB;

    public AtmCorrPanel(AtmCorrPresenter presenter) {
        this.presenter = presenter;

        initComponents();
        bindComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        TableLayout inputTableLayout = new TableLayout(2);
        inputTableLayout.setTableAnchor(TableLayout.Anchor.LINE_START);
        inputTableLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
        inputTableLayout.setColumnWeightX(0, 0.2);
        inputTableLayout.setColumnWeightX(1, 0.8);
        inputTableLayout.setTablePadding(2, 2);
        JPanel inputPanel = new JPanel(inputTableLayout);

        inputPanel.setBorder(BorderFactory.createTitledBorder(null, "Eingabe",
                                                              TitledBorder.DEFAULT_JUSTIFICATION,
                                                              TitledBorder.DEFAULT_POSITION,
                                                              new Font("Tahoma", 0, 11),
                                                              new Color(0, 70, 213)));
        inputPanel.add(new JLabel("Eingabe-Produkt:"));
        JTextField inputProductTextField = new JTextField(presenter.getInputProduct().getName());
        inputProductTextField.setColumns(40);
        inputProductTextField.setEditable(false);
        inputPanel.add(inputProductTextField);
        add(inputPanel, BorderLayout.NORTH);

        TableLayout parameterTableLayout = new TableLayout(3);
        parameterTableLayout.setTableAnchor(TableLayout.Anchor.LINE_START);
        parameterTableLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
        parameterTableLayout.setColumnWeightX(0, 0.2);
        parameterTableLayout.setColumnWeightX(1, 0.4);
        parameterTableLayout.setColumnWeightX(2, 0.4);
        parameterTableLayout.setTablePadding(2, 2);
        JPanel parameterPanel = new JPanel(parameterTableLayout);

        parameterPanel.setBorder(BorderFactory.createTitledBorder(null, "Koeffizienten",
                                                                  TitledBorder.DEFAULT_JUSTIFICATION,
                                                                  TitledBorder.DEFAULT_POSITION,
                                                                  new Font("Tahoma", 0, 11),
                                                                  new Color(0, 70, 213)));

        parameterPanel.add(new JLabel(""));
        final JLabel labelA = new JLabel("Multiplikator a");
        parameterPanel.add(labelA);
        final JLabel labelB = new JLabel("Summand b");
        parameterPanel.add(labelB);
        textFieldsA = new JFormattedTextField[presenter.getBandCount()];
        textFieldsB = new JFormattedTextField[presenter.getBandCount()];
        final DecimalFormat format = new DecimalFormat("#.########");
        for (int i = 0; i < presenter.getBandCount(); i++) {
            parameterPanel.add(new JLabel(presenter.getDisplayBandName(i) + ":"));

            textFieldsA[i] = new JFormattedTextField(format);
            parameterPanel.add(textFieldsA[i]);

            textFieldsB[i] = new JFormattedTextField(format);
            parameterPanel.add(textFieldsB[i]);
        }
        add(parameterPanel, BorderLayout.CENTER);

        TableLayout outputTableLayout = new TableLayout(2);
        outputTableLayout.setTableAnchor(TableLayout.Anchor.LINE_START);
        outputTableLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
        outputTableLayout.setColumnWeightX(0, 0.2);
        outputTableLayout.setColumnWeightX(1, 0.8);
        outputTableLayout.setTablePadding(2, 2);
        JPanel outputPanel = new JPanel(outputTableLayout);

        outputPanel.setBorder(BorderFactory.createTitledBorder(null, "Ausgabe",
                                                               TitledBorder.DEFAULT_JUSTIFICATION,
                                                               TitledBorder.DEFAULT_POSITION,
                                                               new Font("Tahoma", 0, 11),
                                                               new Color(0, 70, 213)));
        outputPanel.add(new JLabel("Ausgabe-Produkt:"));
        final JTextField outputProductTextField = new JTextField(presenter.getOutputProductName());
        outputProductTextField.setColumns(40);
        outputPanel.add(outputProductTextField);
        add(outputPanel, BorderLayout.SOUTH);
    }

    private void bindComponents() {
        for (int i = 0; i < presenter.getCoefficientContainers().length; i++) {
            final SwingBindingContext bindingContext = new SwingBindingContext(presenter.getCoefficientContainer(i));
            bindingContext.bind(textFieldsA[i], "a");
            bindingContext.bind(textFieldsB[i], "b");
        }
    }

}
