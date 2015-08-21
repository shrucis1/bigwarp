package bigwarp.source;

import bdv.viewer.Interpolation;
import bdv.viewer.Source;
import bigwarp.BigWarp.BigWarpData;
import mpicbg.models.AbstractModel;
import mpicbg.models.CoordinateTransform;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class WarpMagnitudeSource< T extends RealType< T >> implements Source< T >
{
	protected final String name;
	
	protected final BigWarpData sourceData;
	
	protected final Interval interval;
	
	protected final WarpMagnitudeRandomAccessibleInterval<T> warpMagImg;
	
	protected T type;
	
	public WarpMagnitudeSource( String name, BigWarpData data, T t  )
	{
		this.name = name;
		this.type = t;
		
		sourceData = data;
		
		RandomAccessibleInterval<?> fixedsrc = data.sources.get( 1 ).getSpimSource().getSource( 0, 0 );
		
		// use the interval of the fixed image
//		if( fixedsrc.dimension( 2 ) == 1 )
//			interval = new FinalInterval( 
//					new long[]{ fixedsrc.min( 0 ), fixedsrc.min( 1 ) }, 
//					new long[]{ fixedsrc.max( 0 ), fixedsrc.max( 1 ) });
//		else
		
		interval = fixedsrc;
		
		warpMagImg = new WarpMagnitudeRandomAccessibleInterval<T>( interval, t, null, null );
	}
	
	public void setWarp( CoordinateTransform warp )
	{
		warpMagImg.ra.warp = warp;
	}
	
	public void setBaseline( AbstractModel<?> baseline )
	{
		warpMagImg.ra.base = baseline;
	}
	
	public AbstractModel<?> getBaseline()
	{
		return warpMagImg.ra.base;
	}
	
	public void debug( double[] pt )
	{
		RealRandomAccess<T> rra = warpMagImg.realRandomAccess();
		
		rra.setPosition( pt );
		
		System.out.println("at ( 0 0 0 ): ");
		System.out.println( "get val: " + rra.get());
		double[] baseRes = warpMagImg.ra.base.apply( pt );
		double[] warpRes = warpMagImg.ra.warp.apply( pt );
		System.out.println( "base res: " + baseRes[0] + " " + baseRes[1]);
		System.out.println( "warp res: " + warpRes[0] + " " + warpRes[1]);
		
	}
	
	public double[] minMax()
	{
		double[] minmax = new double[ 2 ];
		minmax[ 0 ] = Double.MAX_VALUE;
		minmax[ 1 ] = Double.MIN_VALUE;
		
		Cursor<T> curs = Views.iterable( this.getSource( 0,0 ) ).cursor();
		
		while( curs.hasNext() )
		{
			double val = curs.next().getRealDouble();
			if( val < minmax[ 0 ])
				minmax[ 0 ] = val;
			else if( val > minmax[ 1 ])
				minmax[ 1 ] = val;
			
		}
		
		System.out.println( "WarpMag min: " + minmax[ 0 ] + "  max: " + minmax[ 1 ]);
		
		return minmax;
	}
	
	@Override
	public boolean isPresent( int t )
	{
		return ( t == 0 );
	}

	@Override
	public RandomAccessibleInterval<T> getSource( int t, int level ) 
	{
		return Views.interval( Views.raster( 
				getInterpolatedSource( t, level, Interpolation.NEARESTNEIGHBOR ) ), 
				interval );
	}

	@Override
	public RealRandomAccessible<T> getInterpolatedSource( int t, int level, Interpolation method ) 
	{
//		return warpMagImg.copy();
		return warpMagImg;
	}

	@Override
	public void getSourceTransform( int t, int level, AffineTransform3D transform )
	{
		sourceData.sources.get( 0 ).getSpimSource().getSourceTransform( t, level, transform );
	}

	@Override
	public AffineTransform3D getSourceTransform(int t, int level)
	{
		return sourceData.sources.get( 0 ).getSpimSource().getSourceTransform( t, level );
	}

	@Override
	public T getType()
	{
		return type;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public VoxelDimensions getVoxelDimensions()
	{
		return sourceData.seq.getViewSetups().get( 0 ).getVoxelSize();
	}

	@Override
	public int getNumMipmapLevels() 
	{
		return 1;
	}
	
}