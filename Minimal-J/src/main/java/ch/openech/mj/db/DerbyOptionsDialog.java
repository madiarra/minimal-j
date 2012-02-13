/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * StartupFrame.java
 *
 * Created on 17.07.2010, 16:23:00
 */

package ch.openech.mj.db;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JRadioButton;

import ch.openech.mj.swing.component.IndicatingTextField;


/**
 * 
 * @author bruno
 */
public class DerbyOptionsDialog extends javax.swing.JDialog {

	/** Creates new form StartupFrame */
	public DerbyOptionsDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		initComponents();
	}

	public ButtonGroup getChoiceButtonGroup() {
		return choiceButtonGroup;
	}

	public IndicatingTextField getDirectoryTextField() {
		return directoryTextField;
	}

	public JButton getButtonStart() {
		return buttonStart;
	}

	public JRadioButton getButtonDisc() {
		return buttonDisc;
	}

	public JRadioButton getButtonMemory() {
		return buttonMemory;
	}

	public JButton getButtonDirectory() {
		return buttonDirectory;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		choiceButtonGroup = new javax.swing.ButtonGroup();
		buttonMemory = new javax.swing.JRadioButton();
		jLabel1 = new javax.swing.JLabel();
		buttonDisc = new javax.swing.JRadioButton();
		jLabel2 = new javax.swing.JLabel();
		jLabel3 = new javax.swing.JLabel();
		buttonStart = new javax.swing.JButton();
		directoryTextField = new ch.openech.mj.swing.component.IndicatingTextField();
		buttonDirectory = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
		setTitle("Auswahl Speicherung");

		choiceButtonGroup.add(buttonMemory);
		buttonMemory.setFont(buttonMemory.getFont().deriveFont(buttonMemory.getFont().getStyle() | java.awt.Font.BOLD));
		buttonMemory.setSelected(true);
		buttonMemory.setText("Keine dauerhafte Speicherung der Daten");
		buttonMemory.setName("buttonMemory"); // NOI18N

		jLabel1
				.setText("<html>Die Daten werden nur im Arbeitspeicher des PC gehalten und gehen nach beenden der Applikation wieder verloren.<p>(Dies ist die einfachste Option um die Applikation ein erstes Mal auszuprobieren.)</html>");
		jLabel1.setName("jLabel1"); // NOI18N

		choiceButtonGroup.add(buttonDisc);
		buttonDisc.setFont(buttonDisc.getFont().deriveFont(buttonDisc.getFont().getStyle() | java.awt.Font.BOLD));
		buttonDisc.setText("Speicherung in folgendem Verzeichnis:");
		buttonDisc.setName("buttonDisc"); // NOI18N

		jLabel2.setFont(jLabel2.getFont().deriveFont(jLabel2.getFont().getSize() + 4f));
		jLabel2.setText("Auswahl Speicherung");
		jLabel2.setName("jLabel2"); // NOI18N

		jLabel3
				.setText("<html>Mit dieser Option werden die Daten lokal in einem Verzeichnis gespeichert. Damit müssen Sie nicht jedes Mal<br>wieder neue Testdaten anlegen.</html>");
		jLabel3.setName("jLabel3"); // NOI18N

		buttonStart.setText("Anwendung Starten");
		buttonStart.setName("buttonStart"); // NOI18N

		directoryTextField.setName("directoryTextField"); // NOI18N

		buttonDirectory.setText("...");
		buttonDirectory.setName("buttonDirectory"); // NOI18N

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup().addContainerGap().addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jLabel2).addComponent(buttonMemory).addComponent(
								jLabel1).addComponent(buttonDisc).addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 557, Short.MAX_VALUE).addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								layout.createSequentialGroup().addComponent(directoryTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(buttonDirectory))).addContainerGap())
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
						layout.createSequentialGroup().addContainerGap(459, Short.MAX_VALUE).addComponent(buttonStart).addContainerGap()));

		layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] { jLabel1, jLabel3 });

		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup().addContainerGap().addComponent(jLabel2).addGap(18, 18, 18).addComponent(buttonMemory).addPreferredGap(
						javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE,
						javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(
						javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(buttonDisc).addPreferredGap(
						javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(directoryTextField,
								javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(buttonDirectory)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabel3,
						javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(
						javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(buttonStart).addContainerGap(26, Short.MAX_VALUE)));

		pack();
	}// </editor-fold>//GEN-END:initComponents

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new DerbyOptionsDialog(null, true).setVisible(true);
				System.out.println("Und Fertig");
			}
		});
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton buttonDirectory;
	private javax.swing.JRadioButton buttonDisc;
	private javax.swing.JRadioButton buttonMemory;
	private javax.swing.JButton buttonStart;
	private javax.swing.ButtonGroup choiceButtonGroup;
	private ch.openech.mj.swing.component.IndicatingTextField directoryTextField;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	// End of variables declaration//GEN-END:variables

}
