package org.csstudio.opibuilder.detailpanel;

import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.Triangle;
import org.eclipse.draw2d.geometry.Rectangle;

public class DetailPanelFigureRow {

	/* Drawing information */
	private RectangleFigure nameArea;
	private DetailPanelModelRow.Mode mode;
	private DetailPanelModel.DisplayLevel level = DetailPanelModel.DisplayLevel.LOW;
	private DetailPanelFigure figure;
	private DetailPanelDividerFigure divider;
	private Triangle groupTriangle;
	private DetailPanelDraggerFigure dragger;
	private int rowNumber;
	private int visibleRowNumber = 0;
	private Label name;
	private Label tooltip;
	private boolean highlightGroupTriangle = false;
	private boolean groupCollapse = false;
	private boolean collapsed = false;
	private boolean shown = true;
	private boolean editMode = false;
	private boolean waitingForInitialMode = true;
	private static final int groupTriangleSize = 12; 
	private static final int groupTriangleMargin = 2;
	private int top = 0;
	private int bottom = 0;
	
	/* Constructor */
	public DetailPanelFigureRow(DetailPanelFigure f, int n) {
		figure = f;
		rowNumber = n;
		mode = DetailPanelModelRow.Mode.NORMAL;
		// The group triangle
		groupTriangle = new Triangle();
		groupTriangle.setDirection(PositionConstants.SOUTH);
		groupTriangle.setVisible(true);
		figure.addRowWidget(groupTriangle, /*atEnd=*/true);
		// The move icon
		dragger = new DetailPanelDraggerFigure();
		dragger.setCursor(Cursors.SIZEALL);
		figure.addRowWidget(dragger, /*atEnd=*/true);
		// The name label
		name = new Label();
		name.setVisible(true);
		name.setOpaque(false);
		name.setLabelAlignment(PositionConstants.LEFT);
		figure.addRowWidget(name, /*atEnd=*/false);
		// The area covered by the name.  Note this must be added
		// after all the name area components to avoid them being
		// covered by the area rectangle fill.
		nameArea = new RectangleFigure() {
			@Override
			protected void outlineShape(Graphics graphics) {
				// Just draw the right hand border
				Rectangle rect = getBounds();
				graphics.drawLine(rect.x+rect.width-1, rect.y, rect.x+rect.width-1, rect.y+rect.height);
			}
		};
		figure.addRowWidget(nameArea, /*atEnd=*/false);
		// The tooltip
		tooltip = new Label();
		tooltip.setVisible(true);
		tooltip.setOpaque(true);
		tooltip.setLabelAlignment(PositionConstants.LEFT);
		// Set all the colours
		setColor();
		// The horizontal divider that defines the min vertical size
		divider = new DetailPanelDividerFigure(figure.getPane(), /*horizontal=*/false);
		figure.addRowWidget(divider, /*atEnd=*/true);
	}
	
	/* The row is destroyed */
	public void dispose() {
		name.setToolTip(null);
		figure.removeRowWidget(name);
		figure.removeRowWidget(nameArea);
		figure.removeRowWidget(groupTriangle);
		figure.removeRowWidget(divider);
		figure.removeRowWidget(dragger);
	}
	
	/* Return the figure's dragger divider */
	DetailPanelDividerFigure getDraggerDivider() {
		return figure.getDraggerDivider();
	}
	
	/* Set edit mode */
	public void setEditMode(boolean m) {
		editMode = m;
		setVisibility();
	}
	
	/* Return the row number */
	public int getRowNumber() {
		return rowNumber;
	}
	
	/* Return the group collapse state. */
	public boolean getGroupCollapse() {
		return groupCollapse;
	}
	
	/* Set the collapse state of this row */
	public void setCollapse(boolean c) {
		collapsed = c;
		setVisibility();
	}
	
	/* Return true if this row is a group header. */
	public boolean isGroup() {
		return mode == DetailPanelModelRow.Mode.GROUP || mode == DetailPanelModelRow.Mode.COLLAPSEDGROUP;
	}
	
	/* Return true if this row is a collapsed group header. */
	public boolean isCollapsedGroup() {
		return mode == DetailPanelModelRow.Mode.COLLAPSEDGROUP;
	}
	
	/* Return the group triangle object */
	public Shape getGroupTriangle() {
		return groupTriangle;
	}
	
	/* Return the divider object */
	public DetailPanelDividerFigure getDivider() {
		return divider;
	}
	
	/* Return the dragger object */
	public DetailPanelDraggerFigure getDragger() {
		return dragger;
	}
	
	/* Set the divider's position */
	public void setDividerPos(int pos) {
		divider.setPosition(pos);
	}
	
	/* Set the group collapse state */
	public void setGroupCollapse(boolean c) {
		groupCollapse = c;
		if(groupCollapse) {
			groupTriangle.setDirection(PositionConstants.EAST);
		} else {
			groupTriangle.setDirection(PositionConstants.SOUTH);
		}
	}
	
	/* Perform layout actions */
	protected void layout() {
		divider.layout();
	}
	
	/* Set mode */
	public void setMode(DetailPanelModelRow.Mode m) {
		if(!isGroup() && (m == DetailPanelModelRow.Mode.GROUP || m == DetailPanelModelRow.Mode.COLLAPSEDGROUP)) {
			setGroupCollapse(false);
		}
		if(waitingForInitialMode && m == DetailPanelModelRow.Mode.COLLAPSEDGROUP) {
			// Initial state of group collapse
			setGroupCollapse(true);
		}
		waitingForInitialMode = false;
		mode = m;
		setVisibility();
	}
	
	/* Set level */
	public void setLevel(DetailPanelModel.DisplayLevel l) {
		level = l;
		setVisibility();
	}
	
	/* Set the visibility of the components of the row. */
	/* A row is visible if: */
	/*   it is not collapsed */
	/*   it's level is less or equal to the current display level */
	public void setVisibility() {
		name.setVisible(mode != DetailPanelModelRow.Mode.FULLWIDTH && isVisible());
		nameArea.setVisible(mode != DetailPanelModelRow.Mode.FULLWIDTH && isVisible());
		groupTriangle.setVisible(isGroup() && isVisible());
		divider.setVisible(isVisible());
		dragger.setVisible(isVisible() && editMode);
	}
	
	// Return the visibility of the row
	public boolean isVisible() {
		return !collapsed && shown;
	}
	
	/* Set the display state for the group triangle */
	public void setHighlightGroupTriangle(boolean h) {
		highlightGroupTriangle = h;
		if(highlightGroupTriangle) {
			groupTriangle.setBackgroundColor(figure.getRowForegroundColor(visibleRowNumber));
		} else {
			groupTriangle.setBackgroundColor(figure.getRowBackgroundColor(visibleRowNumber));
		}
		groupTriangle.repaint();
	}
	
	/* Set the row name */
	public void setName(String name) {
		this.name.setText(name);
	}
	
	/* Set the row tooltip */
	public void setTooltip(String tip) {
		this.tooltip.setText(tip);
		if(tip == "") {
			name.setToolTip(null);
		} else {
			name.setToolTip(tooltip);
		}
	}
	
	/* One of the colours has changed */
	public void setColor() {
		nameArea.setBackgroundColor(figure.getRowBackgroundColor(visibleRowNumber));
		nameArea.setForegroundColor(figure.getBorderColor());
		name.setForegroundColor(figure.getRowForegroundColor(visibleRowNumber));
		groupTriangle.setForegroundColor(figure.getRowForegroundColor(visibleRowNumber));
		dragger.setForegroundColor(figure.getRowForegroundColor(visibleRowNumber));
	}
	
	/* Set the name drawing area */
	public void setNameArea(Rectangle area) {
		top = area.y;
		bottom = area.y + area.height;
		// The divider's zero is the top of this rectangle
		divider.setOffset(area.y);
		// The overall name rectangle
		Rectangle rect = area;
		nameArea.setBounds(rect);
		// The group triangle
		Rectangle triangleRect = new Rectangle(rect.x+groupTriangleMargin, rect.y+rect.height/2-groupTriangleSize/2, 
				groupTriangleSize, groupTriangleSize);
		groupTriangle.setBounds(triangleRect);
		// The move icon
		Rectangle moveRect;
		if(mode == DetailPanelModelRow.Mode.FULLWIDTH) {
			moveRect = new Rectangle(rect.x, rect.y, DetailPanelDraggerFigure.W, DetailPanelDraggerFigure.W);
		} else {
			moveRect = new Rectangle(rect.x+rect.width-DetailPanelDraggerFigure.W, rect.y, DetailPanelDraggerFigure.W, DetailPanelDraggerFigure.W);
		}
		dragger.setBounds(moveRect);
		// Where the label should be drawn
		rect.setWidth(rect.width - 1 - groupTriangleSize - groupTriangleMargin);
		rect.setX(rect.x + groupTriangleSize + groupTriangleMargin);
		name.setBounds(rect.getShrinked(2, 0));
	}
	
	/* Set the visible row number of the row. */
	public void setVisibleRowNumber(int n) {
		visibleRowNumber = n;
		setColor();
	}
	
	/* Paint the row */
	public void paint(Graphics g) {
		// Does nothing at the moment.
	}
	
	/* Return the level of this row. */
	public DetailPanelModel.DisplayLevel getLevel() {
		return level;
	}
	
	/* Set the shown state of this row */
	public void setShown(boolean s) {
		shown = s;
		setVisibility();
	}
	
	/* Return the distance from the given y coordinate to the top of this row */
	public int distanceToTop(int y) {
		return Math.abs(top - y);
	}
	
	/* Return the distance from the given y coordinate to the bottom of this row */
	public int distanceToBottom(int y) {
		return Math.abs(bottom - y);
	}
}
