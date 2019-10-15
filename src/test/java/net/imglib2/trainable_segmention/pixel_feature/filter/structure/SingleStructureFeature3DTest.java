package net.imglib2.trainable_segmention.pixel_feature.filter.structure;

import ij.ImagePlus;
import net.imagej.ops.OpService;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.Utils;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureInput;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.filter.SingleFeatures;
import net.imglib2.trainable_segmention.pixel_feature.filter.hessian.EigenValues;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import net.imglib2.view.composite.RealComposite;
import org.junit.Test;
import org.scijava.Context;
import trainableSegmentation.ImageScience;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.junit.Assert.assertArrayEquals;

/**
 * Tests {@link SingleStructureFeature3D}.
 */
public class SingleStructureFeature3DTest {

	private final OpService ops = new Context().service(OpService.class);

	@Test
	public void testEigenValues() {
		RealComposite<DoubleType> in = new RealComposite<>(ArrayImgs.doubles(new double[]{1,2,3,4,5,6}, 6).randomAccess(), 6);
		float[] eigenValues = new float[3];
		RealComposite<FloatType> out = new RealComposite<>(ArrayImgs.floats(eigenValues, 3).randomAccess(), 3);
		EigenValues.Vector3D vector3D = new EigenValues.Vector3D();
		SingleStructureFeature3D.eigenValuePerPixel(vector3D, in, out);
		assertArrayEquals(new float[]{11.345f, 0.516f, 0.171f}, eigenValues, 0.001f);
	}

	@Test
	public void testCompareToImageScienceLibrary() {
		// This test gives low PSNR.
		// ImageScience calculates derivatives with slightly to low intensity.
		// The error is increased when calculating the eigenvalues of the squared intensities.
		Img<FloatType> image = ArrayImgs.floats(100, 100, 100);
		Views.interval(image, new FinalInterval(50, 50, 50)).forEach(FloatType::setOne);
		FeatureOp feature = SingleFeatures.structure(1.0, 5.0).newInstance(ops, GlobalSettings.default3d().build());
		Interval target = Intervals.createMinSize(40, 40, 40, 0, 20, 20, 20, 3);
		IntervalView<FloatType> output = createImage(target);
		feature.apply(image, RevampUtils.slices(output));
		RandomAccessibleInterval<FloatType> result2 = asRAI(ImageScience.computeEigenimages(1.0, 5.0, asImagePlusXYZ(image)));
		Utils.showDifference(normalize(output), normalize(Views.interval( result2, target)));
		Utils.assertImagesEqual(18, output, Views.interval( result2, target));
		// The PSNR is much better, when results are normalized to compensate the differently scaled intensities.
		Utils.assertImagesEqual(38, normalize(output), normalize(Views.interval( result2, target)));
	}

	private RandomAccessibleInterval<FloatType> normalize(RandomAccessibleInterval<FloatType> image) {
		double variance = 0;
		for(RealType<?> pixel : Views.iterable(image))
			variance += square( pixel.getRealDouble() );
		double factor = 1 / Math.sqrt(variance);
		return scale( factor, image );
	}

	private RandomAccessibleInterval<FloatType> scale(double factor, RandomAccessibleInterval<FloatType> image) {
		return Converters.convert(image, (i, o) -> { o.set(i); o.mul(factor); }, new FloatType());
	}

	private static double square(double value) {
		return value * value;
	}

	private ImagePlus asImagePlusXYZ(Img<FloatType> image) {
		ImagePlus imp = ImageJFunctions.wrap(image, "").duplicate();
		imp.setStack(imp.getStack(), 1, imp.getStack().size(), 1);
		return imp;
	}

	private RandomAccessibleInterval<FloatType> asRAI(ArrayList<ImagePlus> result) {
		return Views.stack(result.stream().map(ImageJFunctions::wrapFloat).collect(Collectors.toList()));
	}

	@Test
	public void testDerivative() {
		Img<FloatType> image = ArrayImgs.floats(100, 100, 100);
		ops.image().equation(image, "10000 * Math.sin(p[0] / 5) + 2 * p[1] * p[1] + 3 * p[2] * p[2]");
		Interval target = Intervals.createMinSize(40, 40, 40, 20, 20, 20);
		RandomAccessibleInterval<FloatType> result = Views.interval(ImageJFunctions.wrapFloat(ImageScience.computeDifferentialImage(4.0, 0, 0, 1, asImagePlusXYZ(image))), target);
		RandomAccessibleInterval<DoubleType> output = new FeatureInput(image, target).derivedGauss(4.0, 0, 0, 1);
		Utils.assertImagesEqual(100, output, result);
	}

	private static IntervalView<FloatType> createImage(Interval target) {
		return Views.translate(ArrayImgs.floats(Intervals.dimensionsAsLongArray(target)), Intervals.minAsLongArray(target));
	}
}
