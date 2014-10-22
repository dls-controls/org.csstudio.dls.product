package org.csstudio.opibuilder.widgets.detailpanel;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;

public class DetailPanelEditpartRow extends MouseMotionListener.Stub implements MouseListener {
	
	private DetailPanelDividerEditpart divider;
	private DetailPanelDraggerEditpart dragger;
	private DetailPanelFigureRow figure;
	private DetailPanelEditpart editpart;

	public DetailPanelEditpartRow(DetailPanelEditpart e, DetailPanelFigureRow f, DetailPanelModelRow model) {
		figure = f;
		editpart = e;
		divider = new DetailPanelDividerEditpart(editpart, figure.getDivider(), /*horizontal=*/false, 
					DetailPanelModelRow.makePropertyName(DetailPanelModelRow.PROP_ROW_HEIGHT, model.getRowNumber()));
		dragger = new DetailPanelDraggerEditpart(editpart, figure.getDragger(), figure.getDraggerDivider(),
				model.getRowNumber());
		figure.getGroupTriangle().addMouseListener(this);
		figure.getGroupTriangle().addMouseMotionListener(this);
	}
	
	public void dispose() {
		divider.dispose();
		dragger.dispose();
		figure.getGroupTriangle().removeMouseListener(this);
		figure.getGroupTriangle().removeMouseMotionListener(this);
	}

	/* Do nothing with this event. */
	@Override
	public void mouseDoubleClicked(MouseEvent arg0) {
	}

	/* The group triangle has been pressed, change the 
	 * group collapse state.
	 */
	@Override
	public void mousePressed(MouseEvent arg0) {
		editpart.setGroupCollapse(figure.getRowNumber(), !figure.getGroupCollapse());
	}

	/* Do nothing with this event. */
	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

	/* The user has moved the mouse over the group triangle,
	 * give some feedback.
	 */
	@Override
	public void mouseEntered(MouseEvent me) {
		super.mouseEntered(me);
		figure.setHighlightGroupTriangle(true);
	}

	/* The user has moved the mouse off the group triangle,
	 * give some feedback.
	 */
	@Override
	public void mouseExited(MouseEvent me) {
		super.mouseExited(me);
		figure.setHighlightGroupTriangle(false);
	}

	/* Return true if this row is a group header. */
	public boolean isGroup() {
		return figure.isGroup();
	}
	
	/* Return true if this row is a collapsed group header. */
	public boolean isCollapsedGroup() {
		return figure.isCollapsedGroup();
	}
	
	/* Return the group collapse state */
	public boolean getGroupCollapse() {
		return figure.getGroupCollapse();
	}

	/* Set the collapse state of this row */
	public void setCollapse(boolean c) {
		figure.setCollapse(c);
	}
	
	/* Set the shown state of this row */
	public void setShown(boolean s) {
		figure.setShown(s);
	}
}
