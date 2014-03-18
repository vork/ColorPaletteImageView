package com.vork.colorpaletteimageview.Quantizer;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.Arrays;
import java.util.Comparator;

public class DominantColorCalculator {

    private static final String LOG_TAG = DominantColorCalculator.class.getSimpleName();

    private static final int NUM_COLORS = 10;

    private static final int PRIMARY_TEXT_MIN_CONTRAST = 135;

    private static final int SECONDARY_MIN_DIFF_HUE_PRIMARY = 120;

    private static final int TERTIARY_MIN_CONTRAST_PRIMARY = 20;
    private static final int TERTIARY_MIN_CONTRAST_SECONDARY = 90;

    private final MedianCutQuantizer.ColorNode[] mPalette;
    private final MedianCutQuantizer.ColorNode[] mWeightedPalette;
    private ColorScheme mColorScheme;

    public DominantColorCalculator(Bitmap bitmap) {
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();

        final int[] rgbPixels = new int[width * height];
        bitmap.getPixels(rgbPixels, 0, width, 0, 0, width, height);

        final MedianCutQuantizer mcq = new MedianCutQuantizer(rgbPixels, NUM_COLORS);

        mPalette = mcq.getQuantizedColors();
        mWeightedPalette = weight(mPalette);

        findColors();
    }

    public ColorScheme getColorScheme() {
        return mColorScheme;
    }

    private void findColors() {
        final MedianCutQuantizer.ColorNode primaryAccentColor = findPrimaryAccentColor();
        final MedianCutQuantizer.ColorNode secondaryAccentColor = findSecondaryAccentColor(primaryAccentColor);

        final int tertiaryAccentColor = findTertiaryAccentColor(
                primaryAccentColor, secondaryAccentColor);

        final int primaryTextColor = findPrimaryTextColor(primaryAccentColor);
        final int secondaryTextColor = findSecondaryTextColor(primaryAccentColor);

        mColorScheme = new ColorScheme(
                primaryAccentColor.getRgb(),
                secondaryAccentColor.getRgb(),
                tertiaryAccentColor,
                primaryTextColor,
                secondaryTextColor);
    }

    /**
     * @return the first color from our weighted palette.
     */
    private MedianCutQuantizer.ColorNode findPrimaryAccentColor() {
        return mWeightedPalette[0];
    }

    /**
     * @return the next color in the weighted palette which ideally has enough difference in hue.
     */
    private MedianCutQuantizer.ColorNode findSecondaryAccentColor(final MedianCutQuantizer.ColorNode primary) {
        final float primaryHue = primary.getHsv()[0];

        // Find the first color which has sufficient difference in hue from the primary
        for (MedianCutQuantizer.ColorNode candidate : mWeightedPalette) {
            final float candidateHue = candidate.getHsv()[0];

            // Calculate the difference in hue, if it's over the threshold return it
            if (Math.abs(primaryHue - candidateHue) >= SECONDARY_MIN_DIFF_HUE_PRIMARY) {
                return candidate;
            }
        }

        // If we get here, just return the second weighted color
        return mWeightedPalette[1];
    }

    /**
     * @return the first color from our weighted palette which has sufficient contrast from the
     * primary and secondary colors.
     */
    private int findTertiaryAccentColor(final MedianCutQuantizer.ColorNode primary, final MedianCutQuantizer.ColorNode secondary) {
        // Find the first color which has sufficient contrast from both the primary & secondary
        for (MedianCutQuantizer.ColorNode color : mWeightedPalette) {
            if (ColorUtils.calculateContrast(color, primary.getRgb()) >= TERTIARY_MIN_CONTRAST_PRIMARY
                    && ColorUtils.calculateContrast(color, secondary.getRgb()) >= TERTIARY_MIN_CONTRAST_SECONDARY) {
                return color.getRgb();
            }
        }

        // We couldn't find a colour. In that case use the primary colour, modifying it's brightness
        // by 45%
        return ColorUtils.changeBrightness(secondary.getRgb(), 0.45f);
    }

    /**
     * @return the first color which has sufficient contrast from the primary colors.
     */
    private int findPrimaryTextColor(final MedianCutQuantizer.ColorNode primary) {
        // Try and find a colour with sufficient contrast from the primary colour
        for (MedianCutQuantizer.ColorNode color : mPalette) {
            if (ColorUtils.calculateContrast(color, primary.getRgb()) >= PRIMARY_TEXT_MIN_CONTRAST) {
                return color.getRgb();
            }
        }

        // We haven't found a colour, so return black/white depending on the primary colour's
        // brightness
        return ColorUtils.calculateYiqLuma(primary.getRgb()) >= 128 ? Color.BLACK : Color.WHITE;
    }

    /**
     * @return return black/white depending on the primary colour's brightness
     */
    private int findSecondaryTextColor(final MedianCutQuantizer.ColorNode primary) {
        return ColorUtils.calculateYiqLuma(primary.getRgb()) >= 128 ? Color.BLACK : Color.WHITE;
    }

    private static MedianCutQuantizer.ColorNode[] weight(MedianCutQuantizer.ColorNode[] palette) {
        final MedianCutQuantizer.ColorNode[] copy = Arrays.copyOf(palette, palette.length);
        final float maxCount = palette[0].getCount();

        Arrays.sort(copy, new Comparator<MedianCutQuantizer.ColorNode>() {
            @Override
            public int compare(MedianCutQuantizer.ColorNode lhs, MedianCutQuantizer.ColorNode rhs) {
                final float lhsWeight = calculateWeight(lhs, maxCount);
                final float rhsWeight = calculateWeight(rhs, maxCount);

                if (lhsWeight < rhsWeight) {
                    return 1;
                } else if (lhsWeight > rhsWeight) {
                    return -1;
                }
                return 0;
            }
        });

        return copy;
    }

    private static float calculateWeight(MedianCutQuantizer.ColorNode node, final float maxCount) {
        return FloatUtils.weightedAverage(
                ColorUtils.calculateColorfulness(node), 2f,
                (node.getCount() / maxCount), 1f
        );
    }

}
