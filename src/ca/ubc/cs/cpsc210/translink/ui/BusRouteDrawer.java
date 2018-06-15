package ca.ubc.cs.cpsc210.translink.ui;

import android.content.Context;
import ca.ubc.cs.cpsc210.translink.BusesAreUs;
import ca.ubc.cs.cpsc210.translink.model.Route;
import ca.ubc.cs.cpsc210.translink.model.RoutePattern;
import ca.ubc.cs.cpsc210.translink.model.Stop;
import ca.ubc.cs.cpsc210.translink.model.StopManager;
import ca.ubc.cs.cpsc210.translink.util.Geometry;
import ca.ubc.cs.cpsc210.translink.util.LatLon;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// A bus route drawer
public class BusRouteDrawer extends MapViewOverlay {
    /** overlay used to display bus route legend text on a layer above the map */
    private BusRouteLegendOverlay busRouteLegendOverlay;
    /** overlays used to plot bus routes */
    private List<Polyline> busRouteOverlays;

    /**
     * Constructor
     * @param context   the application context
     * @param mapView   the map view
     */
    public BusRouteDrawer(Context context, MapView mapView) {
        super(context, mapView);
        busRouteLegendOverlay = createBusRouteLegendOverlay();
        busRouteOverlays = new ArrayList<>();
    }

    /**
     * Plot each visible segment of each route pattern of each route going through the selected stop.
     */
    public void plotRoutes(int zoomLevel) {
        Polyline polyline;
        List<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
        updateVisibleArea();
        busRouteOverlays.clear(); //Clears the list of polyline
        Stop stop = StopManager.getInstance().getSelected();
        busRouteLegendOverlay.clear(); //Clear the text overlay of the mapping of bus route numbers to colours at the top right corner of the map

        if (!(stop == null))
        {
            for (Route route: stop.getRoutes())
            {
                busRouteLegendOverlay.add(route.getNumber()); //Add the route's number to the legend
                for (RoutePattern routePattern: route.getPatterns())
                {
                    List<LatLon> routePatternPaths = routePattern.getPath();
                    for (int i = 1; i < routePatternPaths.size(); i++)
                    {
                        LatLon pt1 = routePatternPaths.get(i - 1);
                        LatLon pt2 = routePatternPaths.get(i);
                        if (Geometry.rectangleIntersectsLine(northWest, southEast, pt1, pt2)) //Checks if the line formed by the src and destination latLon points intersects the rectangle
                        {
                            polyline = new Polyline(mapView.getContext()); //Creates a new polyline, a list of points where line segments are drawn between latLon Pt1 and latLon Pt2
                            geoPoints.add(new GeoPoint(Geometry.gpFromLL(pt1)));
                            geoPoints.add(new GeoPoint(Geometry.gpFromLL(pt2)));
                            polyline.setPoints(geoPoints); //Sets the points used at either end of the line to draw the line as the list of GeoPoints (List of Pt1 and Pt2)
                            polyline.setWidth(getLineWidth(zoomLevel));
                            polyline.setColor(busRouteLegendOverlay.getColor(route.getNumber()));
                            busRouteOverlays.add(polyline); //Adds the polyline to the list of Polylines
                            geoPoints.clear();
                        }
                    }
                }
            }
        }
    }

    public List<Polyline> getBusRouteOverlays() {
        return Collections.unmodifiableList(busRouteOverlays);
    }

    public BusRouteLegendOverlay getBusRouteLegendOverlay() {
        return busRouteLegendOverlay;
    }


    /**
     * Create text overlay to display bus route colours
     */
    private BusRouteLegendOverlay createBusRouteLegendOverlay() {
        ResourceProxy rp = new DefaultResourceProxyImpl(context);
        return new BusRouteLegendOverlay(rp, BusesAreUs.dpiFactor());
    }

    /**
     * Get width of line used to plot bus route based on zoom level
     * @param zoomLevel   the zoom level of the map
     * @return            width of line used to plot bus route
     */
    private float getLineWidth(int zoomLevel) {
        if(zoomLevel > 14)
            return 7.0f * BusesAreUs.dpiFactor();
        else if(zoomLevel > 10)
            return 5.0f * BusesAreUs.dpiFactor();
        else
            return 2.0f * BusesAreUs.dpiFactor();
    }
}
