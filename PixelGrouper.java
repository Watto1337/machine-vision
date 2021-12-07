import java.util.ArrayList;
import java.util.List;

public class PixelGrouper {

    public static List<PixelGroup> findPixelGroups(Pixel[] pixels, double maxLength) {
        List<PixelGroup> groups = new ArrayList<>();

        for (Pixel pixel : pixels) {
            if (pixel != null) {
                List<PixelGroup> validGroups = null;
                for (PixelGroup group : groups) {
                    if (group.accepts(pixel, maxLength)) {
                        if (validGroups == null) validGroups = new ArrayList<>();
                        validGroups.add(group);
                    }
                }
                if (validGroups == null) {
                    groups.add(new PixelGroup(pixel));
                } else {
                    if (groups.size() > 1) {
                        groups.removeAll(validGroups);
                        validGroups.add(new PixelGroup(pixel));
                        groups.add(new PixelGroup(validGroups));
                    } else {
                        groups.get(0).addPixel(pixel);
                    }
                }
            }
        }

        return groups;
    }

    public static boolean groupsMatch(PixelGroup a, PixelGroup b, double maxRatioDif, double maxMovement) {
        double aWidth = a.getMaxX() - a.getMinX();
        double aHeight = a.getMaxY() - a.getMinY();
        double bWidth = b.getMaxX() - b.getMinX();
        double bHeight = b.getMaxY() - b.getMinY();

        double aRatio = (aWidth) / (aHeight);
        double bRatio = (bWidth) / (bHeight);
        if (Math.abs(aRatio - bRatio) > maxRatioDif) return false;
        double aCenterX = a.getMinX() + aWidth / 2;
        double aCenterY = a.getMinY() + aHeight / 2;
        double bCenterX = b.getMinX() + bWidth / 2;
        double bCenterY = b.getMinY() + bHeight / 2;
        double distance = Math.sqrt(
                Math.pow(aCenterX - bCenterX, 2) + Math.pow(aCenterY - bCenterY, 2)
        );
        return distance <= maxMovement;
    }

}
