package net.imglib2.trainable_segmention.pixel_feature.filter.gauss;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.trainable_segmention.pixel_feature.filter.AbstractFeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureInput;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import preview.net.imglib2.algorithm.gauss3.Gauss3;

import java.util.Collections;
import java.util.List;
/**
 * @author Matthias Arzt
 */
@Plugin(type = FeatureOp.class, label = "Gauss")
public class SingleGaussFeature extends AbstractFeatureOp {

	@Parameter
	private double sigma = 1.0;

	@Override
	public int count() {
			return 1;
		}

	@Override
	public List<String> attributeLabels() {
			return Collections.singletonList("Gaussian_blur_" + sigma);
		}

	@Override
	public void apply(FeatureInput input, List<RandomAccessibleInterval<FloatType>> output) {
		LoopBuilder.setImages(input.gauss(sigma), output.get(0))
				.forEachPixel( (i, o) -> o.setReal(i.getRealFloat()));
	}
}
