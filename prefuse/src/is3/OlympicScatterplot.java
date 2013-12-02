package is3;

import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.AxisLabelLayout;
import prefuse.action.layout.AxisLayout;
import prefuse.controls.PanControl;
import prefuse.controls.ToolTipControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.io.CSVTableReader;
import prefuse.data.io.DataIOException;
import prefuse.render.AxisRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.expression.VisiblePredicate;
import prefuse.visual.sort.ItemSorter;

public class OlympicScatterplot extends Display {
	
	private Table table;
	
	public OlympicScatterplot(String csvfile) {
		
		// Help mainly found at:
		// http://www.ifs.tuwien.ac.at/~rind/w/doku.php/java/prefuse-scatterplot
		
		super(new Visualization());
		
		// Make visualisation easily accessible
		final Visualization vis = this.m_vis;
	    
		table = new Table();
		Schema schema = new Schema();
		
		// Schema allows specifying type of data being stored
		
		schema.addColumn("Country", String.class);
		schema.addColumn("Continent", int.class);
		schema.addColumn("Adult literacy rate (%)", Double.class);
		schema.addColumn("Population (in thousands) total", int.class);
		schema.addColumn("Population median age (years)", int.class);
		schema.addColumn("Community and traditional health workers density (per 10 000 population)", Double.class);
		schema.addColumn("General government expenditure on health as percentage of total expenditure on health", int.class);
		schema.addColumn("General government expenditure on health as percentage of total government expenditure", int.class);
		schema.addColumn("F2012", int.class);
		schema.addColumn("M2012", int.class);
		schema.addColumn("TeamSize", int.class);
		schema.addColumn("Gold", int.class);
		schema.addColumn("Silver", int.class);
		schema.addColumn("Bronze", int.class);
		
		table.addColumns(schema);
		
		/* this is me just randomly putting 200 ssince I know there isnt more than 200 countries, need to change this */
		table.addRows(200);
		
		// Read table from csv file
		try {
			table = new CSVTableReader().readTable(csvfile);
		}
		catch (DataIOException e) {
			e.printStackTrace();
	        System.err.println("Error loading table. Exiting...");
	        System.exit(1);
		}
		
		final Rectangle2D boundsData = new Rectangle2D.Double();
		final Rectangle2D boundsLabelsX = new Rectangle2D.Double();
		final Rectangle2D boundsLabelsY = new Rectangle2D.Double();
	    
		// ------------------------------------------------------------------
		// Step 1: setup the visualised data
		
		VisualTable vt = vis.addTable("data", table);
		
		vt.addColumn("label", "CONCAT([Country],': ', ' Gold: ', [Gold], ';  Silver: ', [Silver], ';  Bronze: ', [Bronze])");
		
		// ------------------------------------------------------------------
		// Step 2: setup renderers for visualised data
		
		DefaultRendererFactory rf = new DefaultRendererFactory(); 
		
		// Render labels on x & y axis
		rf.add(new InGroupPredicate("ylab"), 
				new AxisRenderer(Constants.FAR_LEFT, Constants.CENTER));
		rf.add(new InGroupPredicate("xlab"), 
				new AxisRenderer(Constants.CENTER, Constants.FAR_BOTTOM));
		vis.setRendererFactory(rf);
		
		// ------------------------------------------------------------------
		// Step 3: create actions to process the visual data
		
		AxisLayout x_axis = new AxisLayout("data", "TeamSize", Constants.X_AXIS, VisiblePredicate.TRUE);
		AxisLayout y_axis = new AxisLayout("data", "Gold", Constants.Y_AXIS, VisiblePredicate.TRUE);
		
		x_axis.setLayoutBounds(boundsData);
		y_axis.setLayoutBounds(boundsData);
		
		AxisLabelLayout x_labels = new AxisLabelLayout("xlab", x_axis, boundsLabelsX);
		AxisLabelLayout y_labels = new AxisLabelLayout("ylab", y_axis, boundsLabelsY);
		
		// Colour palette for continents
		int[] palette = new int[] {
				// Middle East
	            ColorLib.rgba(20,120,100,125),
	            // Europe
	            ColorLib.rgba(105,245,40,125),
	            // Africa
	            ColorLib.rgba(200,200,135,125),
	            // North America
	            ColorLib.rgba(35,50,225,125),
	            // South America
	            ColorLib.rgba(190,115,20,125),
	            // Oceania & some Asia
	            ColorLib.rgba(95,175,210,125),
	            // Asia
	            ColorLib.rgba(220,15,40,125)
	        };
		
		DataColorAction fill = new DataColorAction("data", "Continent", Constants.NOMINAL, VisualItem.FILLCOLOR, palette);
		
		ActionList draw = new ActionList();
		draw.add(x_axis);
		draw.add(y_axis);
		draw.add(x_labels);
		draw.add(y_labels);
		draw.add(fill);
		// draw.add(new RepaintAction());
		vis.putAction("draw", draw);
		
		ActionList update = new ActionList();
		update.add(x_axis);
		update.add(y_axis);
		update.add(x_labels);
		update.add(y_labels);
		update.add(fill);
		// draw.add(new RepaintAction());
		vis.putAction("update", update);

		// ------------------------------------------------------------------
		// Step 4: Setup a display and controls

		// set display size
		setSize(720, 500);
	    // pan with left-click drag on background
	    addControlListener(new PanControl()); 
	    // zoom with right-click drag
	    addControlListener(new ZoomControl());
	    
	    // Update visuals when user resizes display
	    addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				updateBounds(boundsData, boundsLabelsX, boundsLabelsY);
				vis.run("update");
			}
		});
		
		setItemSorter(new ItemSorter() {
			public int score(VisualItem item) {
				int score = super.score(item);
				if (item.isInGroup("data"))
					score++;
				return score;
			}
		});
		 
	    // Display specific information when hovering over point
		ToolTipControl ttc = new ToolTipControl("label");
		addControlListener(ttc);
		
		// ------------------------------------------------------------------
		// Step 5: Launching the visualisation

		updateBounds(boundsData, boundsLabelsX, boundsLabelsY);
		vis.run("draw");
	}
	
	/**
	 * Taken from http://www.ifs.tuwien.ac.at/~rind/w/doku.php/java/prefuse-scatterplot
	 * 
	 * @param boundsData
	 * @param boundsLabelsX
	 * @param boundsLabelsY
	 */
	private void updateBounds(Rectangle2D boundsData, Rectangle2D boundsLabelsX, Rectangle2D boundsLabelsY) {

		int paddingLeft = 30;
		int paddingTop = 15;
		int paddingRight = 30;
		int paddingBottom = 15;

		int axisWidth = 20;
		int axisHeight = 10;

		Insets i = getInsets();

		int left = i.left + paddingLeft;
		int top = i.top + paddingTop;
		int innerWidth = getWidth() - i.left - i.right - paddingLeft - paddingRight;
		int innerHeight = getHeight() - i.top - i.bottom - paddingTop - paddingBottom;

		boundsData.setRect(left + axisWidth, top, innerWidth - axisWidth,
				innerHeight - axisHeight);
		boundsLabelsX.setRect(left + axisWidth, top + innerHeight - axisHeight,
				innerWidth - axisWidth, axisHeight);
		boundsLabelsY.setRect(left, top, innerWidth + paddingRight, innerHeight
				- axisHeight);
	}
	
}