/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2016 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Jean-Yves Tinevez and Michael Zinsmaier.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package preview.net.imglib2.algorithm.neighborhood;

import java.util.Arrays;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.Shape;

/**
 * A factory for Accessibles on hyper-sphere neighboorhoods.
 *
 * @author Tobias Pietzsch
 * @author Jonathan Hale (University of Konstanz)
 */
public class HyperEllipsoidShape implements Shape
{
	private final long[] radius;

	public HyperEllipsoidShape( final long[] radius )
	{
		this.radius = radius;
	}

	@Override
	public < T > IterableInterval< Neighborhood< T > > neighborhoods( final RandomAccessibleInterval< T > source )
	{
		return new NeighborhoodIterableInterval< T >( source, HyperEllipsoidNeighborhoodUnsafe.< T >factory( radius ) );
	}

	@Override
	public < T > RandomAccessible< Neighborhood < T > > neighborhoodsRandomAccessible( final RandomAccessible< T > source )
	{
		return new NeighborhoodRandomAccessible< T >( source, HyperEllipsoidNeighborhoodUnsafe.< T >factory( radius ) );
	}

	@Override
	public < T > IterableInterval< Neighborhood< T > > neighborhoodsSafe( final RandomAccessibleInterval< T > source )
	{
		return new NeighborhoodIterableInterval< T >( source, HyperEllipsoidNeighborhood.< T >factory( radius ) );
	}

	@Override
	public < T > RandomAccessible< Neighborhood< T > > neighborhoodsRandomAccessibleSafe( final RandomAccessible< T > source )
	{
		return new NeighborhoodRandomAccessible< T >( source, HyperEllipsoidNeighborhood.< T >factory( radius ) );
	}

	/**
	 * @return The radius of this shape.
	 */
	public long[] getRadius()
	{
		return radius;
	}

	@Override
	public String toString()
	{
		return "HyperEllipsoidShape, radius = " + radius;
	}

	@Override
	public Interval getStructuringElementBoundingBox(final int numDimensions) {
		final long[] min = new long[numDimensions];
		Arrays.setAll(min,  i -> -radius[i]);
		final long[] max = radius;

		return new FinalInterval(min, max);
	}
}
