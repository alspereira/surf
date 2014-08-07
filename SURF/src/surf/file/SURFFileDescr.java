/*
 *  AudioFileDescr.java
 *  (ScissLib)
 *
 *  Copyright (c) 2004-2013 Hanns Holger Rutz. All rights reserved.
 *
 *	This library is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU Lesser General Public
 *	License as published by the Free Software Foundation; either
 *	version 2.1 of the License, or (at your option) any later version.
 *
 *	This library is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *	Lesser General Public License for more details.
 *
 *	You should have received a copy of the GNU Lesser General Public
 *	License along with this library; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package surf.file;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
//import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
//import java.util.Locale;
import java.util.Map;
import java.util.Set;

//import de.sciss.gui.StringItem;

/**
 *  An <code>AudioFileDescr</code> is
 *  a data structure that describes the
 *  format of an <code>AudioFile</code>.
 *  It was public readable fields for
 *  common parameters such as sample rate
 *  and bitdepth. More specific features
 *  such as markers or gain chunks
 *  are stored using a <code>Map</code> object
 *  being accessed throught the <code>setProperty</code>
 *  and <code>getProperty</code> methods.
 *  A corresponding GUI element,
 *  the <code>AudioFileFormatPane</code> exists
 *  which presents the common fields to the user.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.29, 10-Sep-08
 *
 *  @see		AudioFile
 *  @see		AudioFileFormatPane
 *
 *  @todo		all files are considered big endian at the
 *				moment which might be inconvenient on non
 *				mac systems because there are for example
 *				different endian versions for ircam and we
 *				cannot read the little endian one. aiff however
 *				is per se big endian and should therefore cause
 *				no trouble.
 */
public class SURFFileDescr {
	// -------- public Variables --------
	/**
	 *  type value : undefined audio file format
	 */
	public static final int TYPE_UNKNOWN	= -1;
	/**
	 *  type value : wave (riff) sound file format
	 */
	public static final int TYPE_WAVE		= 0;
	
	private static final int NUM_TYPES		= 1;

	/**
	 *  sampleFormat type : linear pcm integer
	 */
	public static final int FORMAT_INT		= 0;
	/**
	 *  sampleFormat type : pcm floating point
	 */
	public static final int FORMAT_FLOAT	= 1;

	/**
	 *	This denotes a corresponding
	 *	file on a harddisk. It may be null
	 */
	public File		file;

	// ---- fields supported by all formats ----
	
	/**
	 *  file format such as TYPE_AIFF
	 */
	public int		type;
	/**
	 *  number of channels (interleaved)
	 */
	public int		channels;
	/**
	 *  sampling rate in hertz
	 */
	public double	rate;
	/**
	 *  bits per sample
	 */
	public int		bitsPerSample;
	/**
	 *  sample number format, FORMAT_INT or FORMAT_FLOAT
	 */
	public int		sampleFormat;
	/**
	 *  sound file length in sample frames
	 */
	public long		length;			// in sampleframes
	/**
	 *  timestamp of the first sample in the file.
	 *  Format: YYYY-MM-DD hh:mm:ss:mmm
	 */
	public String	SURF_initial_timestamp = "";
	/**
	 *  timezone of the place where the dataset 
	 *  was collected (e.g. EST, PST, or whatever makes sense for you)
	 */
	public String	SURF_timezone = "";
	/**
	 *  sample rate in hertz (this exists to overcome the limitation that 
	 *  wave format only supports integer rates greater or equal to 1 Hz)
	 */
	public float	SURF_sample_rate = 0f;
	
	public float[] 	SURF_channel_calibration;
	
	/**
	 *  property key : label list. value class = (java.util.)List whose elements are of class Marker
	 *
	 *  @see	de.sciss.io.Marker
	 */
	public static final String KEY_LABELS  =   "labels";
	/**
	 *  property key : note list. value class = (java.util.)List whose elements are of class Marker
	 *
	 *  @see	de.sciss.io.Marker
	 */
	public static final String KEY_NOTES = "notes";
	/**
	 *  property key : region list. value class = (java.util.)List whose elements are of class Region
	 *
	 *  @see	de.sciss.io.Region
	 */
	public static final String KEY_REGIONS  =   "regions";
	/**
	 *  property key : custom comment list. value class = (java.util.)List whose elements are of class Annotation
	 *  
	 *  @see	de.sciss.io.Annotation  
	 */
	public static final String KEY_COMMENTS	=   "comment";
	/**
	 *  property key : custom metadata list. value class = (java.util.)List whose elements are of class Annotation
	 *  
	 *  @see	de.sciss.io.Annotation  
	 */
	public static final String KEY_METADATA = "metadata";
	/**
	 *  property key : default metadata. value class = Info
	 *  
	 *  @see	de.sciss.io.Info
	 */
	public static final String KEY_INFO = "info";
	

	// -------- protected Variables --------

	private final Map<String, Object> properties;
	
	@SuppressWarnings("rawtypes")
	private static final Set[] supports;
	
	private List<PropertyChangeListener>	pcs	= null;
	
	private static final String[] FORMAT_SUFFICES	= { "wav", "raw" };

	//private static final String			msgPtrn		= "{0,choice,0#AIFF|1#NeXT/Sun AU|2#IRCAM|3#WAVE|4#Raw|5#Wave64} audio, {1,choice,0#no channels|1#mono|2#stereo|2<{1,number,integer}-ch} {2,number,integer}-bit {3,choice,0#int|1#float} {4,number,0.###} kHz, {5,number,integer}:{6,number,00.000}";
	//private static final MessageFormat	msgForm		= new MessageFormat( msgPtrn, Locale.US );  // XXX US locale to allow parsing via Double.parseDouble()
											
	static {
		Set<String>	set;
		supports				= new Set[ NUM_TYPES ];
		
		set						= new HashSet<String>( 6 );
		set.add( KEY_LABELS );
		set.add( KEY_NOTES );
		set.add( KEY_REGIONS );
		set.add( KEY_COMMENTS );
		set.add( KEY_METADATA );
		set.add( KEY_INFO );
		supports[ TYPE_WAVE ]	= set;
	}

	// -------- public Methods --------

	/**
	 *  Construct a new <code>SURFFileDescr</code>
	 *  whose fields are all undefined
	 */
	public SURFFileDescr() {
		properties =   new HashMap<String, Object>();
	}
	
	/**
	 *  Construct a new <code>SURFFileDescr</code>
	 *  whose common fields are copied from a
	 *  template (type, channels, rate, bitsPerSample,
	 *  sampleFormat, length, properties, surf_timestamp,
	 *  surf_timezone, surf_sample_rate, surf_channel_callibration).
	 *
	 *  @param  orig	a pre-existing description whose
	 *					values will be copied to the newly
	 *					constructed description
	 *
	 *	@warning	things like the marker list are not duplicated,
	 *				they refer to the same instance
	 */
	public SURFFileDescr( SURFFileDescr orig ) {
		this.file			= orig.file;
		this.type			= orig.type;
		this.channels		= orig.channels;
		this.rate			= orig.rate;
		this.bitsPerSample  = orig.bitsPerSample;
		this.sampleFormat   = orig.sampleFormat;
		this.length			= orig.length;
		synchronized( orig.properties ) {
			this.properties		= new HashMap<String, Object>( orig.properties );
		}
		// SURF specific properties
		this.SURF_initial_timestamp 	= orig.SURF_initial_timestamp;
		this.SURF_timezone				= orig.SURF_timezone;
		this.SURF_sample_rate			= orig.SURF_sample_rate;
		this.SURF_channel_calibration 	= orig.SURF_channel_calibration;
	}
	
	/**
	 *  Returns the file format type
	 *
	 *  @return the type of the file, e.g. TYPE_WAVE
	 */
	public int getType() {
		return type;
	}

	/**
	 *  Gets a specific property
	 *
	 *  @param  	key the key of the property to query,
	 *				such as KEY_LABELS
	 *  @return		the property's value or null
	 *				if this property doesn't exist.
	 *				the class of the property varies
	 *				depending on the property type. see
	 *				the key's description to find out what
	 *				kind of object is returned
	 */
	public Object getProperty( Object key ) {
		synchronized( properties ) {
			return( properties.get( key ));
		}
	}

	/**
	 *  Sets a specific property. Use the
	 *  <code>isPropertySupported</code> method
	 *  to find out if the chosen file format can store
	 *  the property.
	 *
	 *  @param  key		the key of the property to set
	 *  @param  value   the properties value. Note that the
	 *					value is not checked at all. It is the
	 *					callers responsibility to ensure the value's
	 *					class is the one specified for the particular key.
	 *
	 *  @see	#isPropertySupported( String )
	 */
	public void setProperty( String key, Object value ) {
		synchronized( properties ) {
			properties.put( key, value );
		}
	}

	/**
	 *  Sets a specific property and dispatches
	 *	a <code>PropertyChangeEvent</code> to registered listeners
	 *
	 *  @param  key		the key of the property to set
	 *  @param  value   the properties value.
	 *
	 *  @see	#addPropertyChangeListener( PropertyChangeListener )
	 *
	 *	@synchronization	must be called in the event thread
	 */
	public void setProperty( Object source, String key, Object value ) {
		synchronized( properties ) {
			final Object oldValue = properties.put( key, value );
			if( (source != null) && (pcs != null) ) {
				final PropertyChangeEvent e = new PropertyChangeEvent( source, key, oldValue, value );
				// the rude way
				for( int i = 0; i < pcs.size(); i++ ) {
					pcs.get( i ).propertyChange( e );
				}
			}
		}
	}
	
	public void addPropertyChangeListener( PropertyChangeListener l ) {
		synchronized( properties ) {
			if( pcs == null ) {
				pcs = new ArrayList<PropertyChangeListener>();
			}
			pcs.add( l );
		}
	}

	public void removePropertyChangeListener( PropertyChangeListener l ) {
		synchronized( properties ) {
			if( pcs == null ) {
				pcs.remove( l );
			}
		}
	}

	/**
	 *  Reports if a sound file format can handle
	 *  a particular property.
	 *
	 *  @param  key		the key of the property to check
	 *  @return			<code>true</code> the sound file format
	 *					given by <code>getType()</code> supports
	 *					the property. <code>false</code> if not.
	 *					Note that if a property is not supported,
	 *					it is no harm to set it using <code>setProperty</code>,
	 *					it just won't be written to the sound file's header.
	 */
	public boolean isPropertySupported( String key ) {
		if( type >= 0 && type < supports.length ) {
			return supports[ type ].contains( key );
		} else {
			return false;
		}
	}

	/**
	 *  Gets the suffix commonly
	 *	used for attaching to a file name of
	 *	the given format.
	 *
	 *	@param	type	format such as TYPE_WAVE, TYPE_RAW etc.
	 *
	 *  @return the suffix string such as "aif", "raw"
	 *			or <code>null</code> if the type was invalid.
	 */
	public static String getFormatSuffix( int type ) {
		if( (type >= 0) && (type < FORMAT_SUFFICES.length) ) {
			return FORMAT_SUFFICES[ type ];
		} else {
			return null;
		}
	}

	/**
	 *  Utility method to convert milliseconds to sample frames
	 *  according to the given audio file format
	 *
	 *  @param  afd		the audio file description whose
	 *					rate field is used to do the conversion
	 *  @param  ms		arbitrary offset in milliseconds. note
	 *					that this doesn't have to be within the
	 *					range of the current length of the audio file.
	 *  @return the time offset which was specified in milliseconds,
	 *			converted to sample frames (round to integer value if needed).
	 */
	public static double millisToSamples( SURFFileDescr afd, double ms ) {
		return( (ms / 1000) * afd.rate );
	}

	/**
	 *  Utility method to convert sample frames to milliseconds
	 *  according to the given audio file format
	 *
	 *  @param  afd		the audio file description whose
	 *					rate field is used to do the conversion
	 *  @param  samples	arbitrary offset in sample frames. note
	 *					that this doesn't have to be within the
	 *					range of the current length of the audio file.
	 *  @return the time offset which was specified in sample frames,
	 *			converted to milliseconds (round to integer value if needed).
	 */
	public static double samplesToMillis( SURFFileDescr afd, long samples )
	{
		return( samples / afd.rate * 1000 );
	}
}
// class AudioFileDescr