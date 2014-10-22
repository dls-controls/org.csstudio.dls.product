package org.csstudio.opibuilder.widgets.detailpanel;

import org.eclipse.gef.commands.Command;

// A command object that changes the location of a row in the table
public class DetailPanelChangeRowIndexCommand extends Command {
	
	private int oldIndex = 0;
	private int newIndex = 0;
	private DetailPanelEditpart editpart;

	public DetailPanelChangeRowIndexCommand(DetailPanelEditpart editpart, int oldIndex, int newIndex) {
		this.oldIndex = oldIndex;
		this.newIndex = newIndex;
		this.editpart = editpart;
		setLabel("Change row location");
	}
	
	@Override
	public void execute() {
		editpart.moveRow(oldIndex, newIndex);
	}
	
	@Override
	public void undo() {
		if(oldIndex < newIndex) {
			editpart.moveRow(newIndex-1, oldIndex);
		} else {
			editpart.moveRow(newIndex, oldIndex+1);
		}
	}
}
