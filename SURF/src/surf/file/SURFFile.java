/*
 *  AudioFile.java
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

//import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//import de.sciss.app.AbstractApplication;

/**
 *	The <code>AudioFile</code> allows reading and writing
 *	of sound files. It wraps a <code>RandomAccessFile</code>
 *	and delegates the I/O to subclasses which deal with
 *	the specific sample format and endianess.
 *	<p>
 *	Currently supported formats are: AIFF, IRCAM,
 *  NeXT/Sun (.au), WAVE, and Wave64. Supported resolutions are
 *  8/16/24/32 bit integer and 32/64 bit floating point.
 *  However not all audio formats support all bit depths.
 *  <p>
 *	Not all format combinations are supported, for example
 *	the rather exotic little-endian AIFF, but also
 *	little-endian SND, WAVE 8-bit.
 *	<p>
 *  In order to simplify communication with CSound,
 *  raw output files are supported, raw input files however
 *  are not recognized.
 *  <p>
 *  To create a new <code>AudioFile</code> you call
 *  one of its static methods <code>openAsRead</code> or
 *  <code>openAsWrite</code>. The format description
 *  is handled by an <code>AudioFileDescr</code> object.
 *	This object also contains information about what special
 *	tags are read/written for which format. For example,
 *	AIFF can read/write markers, and application-specific
 *	chunk, and a gain tag. WAVE can read/write markers and
 *	regions, and a gain tag, etc.
 *	<p>
 *	The <code>AudioFile</code> implements the generic
 *	interface <code>InterleavedStreamFile</code> (which
 *	is likely to be modified in the future) to allow
 *	clients to deal more easily with different sorts
 *	of streaming files, not just audio files.
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.38, 26-Jun-09
 *
 *  @see		AudioFileDescr
 *
 *  @todo		more flexible handling of endianess,
 *				at least SND and IRCAM should support both
 *				versions.
 *
 *	@todo		more tags, like peak information and
 *				channel panning.
 *
 *	@todo		(faster) low-level direct file-to-file
 *				copy in the copyFrames method
 */
public class SURFFile
implements InterleavedStreamFile {
	private static final int MODE_READONLY   = 0;
	private static final int MODE_READWRITE  = 1;

	protected final RandomAccessFile	raf;
	protected final FileChannel			fch;
	private final int					mode;

	protected SURFFileDescr				afd;
	private AudioFileHeader				afh;
	
	protected ByteBuffer				byteBuf;
	private int							byteBufCapacity;
	protected int						bytesPerFrame;
	protected int						frameBufCapacity;
	private BufferHandler				bh;
	protected int						channels;
	private long						framePosition;
	
	private long						updateTime;
	private long						updateLen;
	private long						updateStep;
	
	// -------- public Methods --------

	/**
	 *  Opens an audio file for reading.
	 *
	 *  @param		f   the path name of the file
	 *  @return		a new <code>AudioFile</code> object
	 *				whose header is already parsed and can
	 *				be obtained through the <code>getDescr</code> method.
	 *
	 *  @throws IOException if the file was not found, could not be read
	 *						or has an unknown or unsupported format
	 */
	public static SURFFile openAsRead( File f )
	throws IOException {
		final SURFFile sf	= new SURFFile( f, MODE_READONLY );
		sf.afd				= new SURFFileDescr();
		sf.afd.file			= f;
		sf.afd.type			= sf.retrieveType();
		sf.afh				= sf.createHeader();
		sf.afh.readHeader( sf.afd );
		sf.init();
		sf.seekFrame( 0 );
		return sf;
	}
	
	/**
	 *  Opens an audio file for reading/writing. The pathname
	 *	is determined by the <code>file</code> field of the provided <code>SURFFileDescr</code>.
	 *	If a file denoted by this path already exists, it will be
	 *	deleted before opening.
	 *	<p>
	 *	Note that the initial audio file header is written immediately.
	 *	Special tags for the header thus need to be set in the <code>SURFFileDescr</code>
	 *	before calling this method, including markers and regions. It is not
	 *	possible to write markers and regions after the file has been opened
	 *	(since the header size has to be constant).
	 *
	 *  @param  afd format and resolution of the new audio file.
	 *				the header is immediately written to the hard-disc
	 *
	 *  @throws IOException if the file could not be created or the
	 *						format is unsupported
	 */
	public static SURFFile openAsWrite( SURFFileDescr afd )
	throws IOException {
		if( afd.file.exists() ) afd.file.delete();
		final SURFFile sf	= new SURFFile( afd.file, MODE_READWRITE );
		sf.afd				= afd;
		afd.length			= 0;
		sf.afh				= sf.createHeader();
		sf.afh.writeHeader( sf.afd );
		sf.init();
		sf.seekFrame( 0 );
		sf.updateStep		= (long) afd.rate * 20;
		sf.updateLen		= sf.updateStep;
		sf.updateTime		= System.currentTimeMillis() + 10000;
		return sf;
	}
	
	/**
	 *  Determines the type of audio file.
	 *
	 *  @param		f   the path name of the file
	 *  @return		the type code as defined in <code>SURFFileDescr</code>,
	 *				e.g. <code>TYPE_AIFF</code>. Returns <code>TYPE_UNKNOWN</code>
	 *				if the file could not be identified.
	 *
	 *  @throws IOException if the file could not be read
	 */
	public static int retrieveType( File f )
	throws IOException {
		final SURFFile sf		= new SURFFile( f, MODE_READONLY );
		final int		type	= sf.retrieveType();
		sf.cleanUp();
		return type;
	}
	
	private SURFFile( File f, int mode )
	throws IOException {
		raf			= new RandomAccessFile( f, mode == MODE_READWRITE ? "rw" : "r" );
		fch			= raf.getChannel();
		this.mode   = mode;
	}

	/**
	 *  Returns a description of the audio file's format.
	 *  Fields which are guaranteed to be filled in, are
	 *  the type (use <code>getType</code>), <code>channels</code>,
	 *  <code>bitsPerSample</code>, <code>sampleFormat</code>,
	 *  <code>rate</code> and <code>length</code>.
	 *
	 *  @return an <code>AudioFileDescr</code> describing
	 *			this audio file.
	 *
	 *  @warning	the returned description is not immutable but
	 *				should be considered read only, do not modify it.
	 *				the fields may change dynamically if the file
	 *				is modified, e.g. the <code>length</code> field
	 *				for a writable file.
	 */
	public SURFFileDescr getDescr() {
		return afd;
	}
	
	/**
	 *  Returns the file that was used to open
	 *  the audio file. Note that this simply returns
	 *	getDescr().file, so it's not a good idea to
	 *	modify this field after opening the audio file.
	 *
	 *  @return the <code>File</code> that was used in
	 *			the static constructor methods. Can be used
	 *			to query the pathname or to delete the file after
	 *			it has been closed
	 */
	public File getFile() {
		return afd.file;
	}
	
	private void init() throws IOException {
		channels		= afd.channels;
		bytesPerFrame	= (afd.bitsPerSample >> 3) * channels;
		frameBufCapacity= Math.max( 1, 65536 / Math.max( 1, bytesPerFrame ));
		byteBufCapacity = frameBufCapacity * bytesPerFrame;
		byteBuf			= ByteBuffer.allocateDirect( byteBufCapacity );
		byteBuf.order( afh.getByteOrder() );
		bh				= null;

		switch( afd.sampleFormat ) {
		case SURFFileDescr.FORMAT_INT:
			switch( afd.bitsPerSample ) {
			case 8:			// 8 bit int
				if( afh.isUnsignedPCM() ) {
					bh  = new UByteBufferHandler();
				} else {
					bh  = new ByteBufferHandler();
				}
				break;
			case 16:		// 16 bit int
				bh  = new ShortBufferHandler();
				break;
			case 24:		// 24 bit int
				if( afh.getByteOrder() == ByteOrder.BIG_ENDIAN ) {
					bh  = new ThreeByteBufferHandler();
				} else {
					bh  = new ThreeLittleByteBufferHandler();
				}
				break;
			case 32:		// 32 bit int
				bh  = new IntBufferHandler();
				break;
			}
			break;
		case SURFFileDescr.FORMAT_FLOAT:
			switch( afd.bitsPerSample ) {
			case 32:		// 32 bit float
				bh  = new FloatBufferHandler();
				break;
			case 64:		// 64 bit float
				bh  = new DoubleBufferHandler();
				break;
			}
		}
		if( bh == null) throw new IOException( getResourceString( "errAudioFileEncoding" ));
	}

	private AudioFileHeader createHeader() throws IOException {
		switch( afd.getType() ) {
		case SURFFileDescr.TYPE_WAVE:
			return new WAVEHeader();
		default:
			throw new IOException( getResourceString( "errAudioFileType" ));
		}
	}

	/*
	 *	Reads file header in order to determine file type
	 */
	private int retrieveType() throws IOException {
		long	len		= raf.length();
		long	oldpos	= raf.getFilePointer();
		int		magic;
		int		type	= SURFFileDescr.TYPE_UNKNOWN;

		if( len < 4 ) return type;

		raf.seek( 0L );
		magic = raf.readInt();
		switch( magic ) {
		case WAVEHeader.RIFF_MAGIC:					// -------- probably WAVE --------
			if( len < 12 ) break;
			raf.readInt();
			magic = raf.readInt();
			switch( magic ) {
			case WAVEHeader.WAVE_MAGIC:
				type = SURFFileDescr.TYPE_WAVE;
				break;
			}
			break;
		default:
			break;
		}

		raf.seek( oldpos );
		return type;
	}

	/**
	 *  Moves the file pointer to a specific
	 *  frame.
	 *
	 *  @param  frame   the sample frame which should be
	 *					the new file position. this is really
	 *					the sample index and not the physical file pointer.
	 *  @throws IOException when a seek error occurs or you try to
	 *						seek past the file's end.
	 */
	public void seekFrame( long frame ) throws IOException {
		long physical	= afh.getSampleDataOffset() + frame * bytesPerFrame;
		raf.seek( physical );
		framePosition = frame;
	}
	
	/**
	 *	Flushes pending buffer content, and 
	 *	updates the sound file header information
	 *	(i.e. length fields). Usually you
	 *	will not have to call this method directly,
	 *	unless you pause writing for some time
	 *	and want the file information to appear
	 *	as accurate as possible.
	 */
	public void flush() throws IOException {
		updateTime	= System.currentTimeMillis() + 10000;
		afd.length	= framePosition;
		afh.updateHeader( afd );
		updateLen	= framePosition + updateStep;
		fch.force( true );
	}
	
	/**
	 *  Returns the current file pointer in sample frames
	 *
	 *  @return		the sample frame index which is the offset
	 *				for the next read or write operation.
	 *
	 *  @throws IOException		when the position cannot be queried
	 */
	public long getFramePosition() throws IOException {
		return( framePosition );
	}

	/**
	 *	Reads sample frames from the current position
	 *
	 *  @param  data	buffer to hold the frames read from hard-disc.
	 *					the samples will be de-interleaved such that
	 *					data[0][] holds the first channel, data[1][]
	 *					holds the second channel etc.
	 *					; it is allowed to have null arrays in the data
	 *					(e.g. data[0] == null), in which case these channels
	 *					are skipped when reading
	 *  @param  offset  offset in the buffer in sample frames, such
	 *					that the first frame of the first channel will
	 *					be placed in data[0][offset] etc.
	 *  @param  length  number of continuous frames to read.
	 *
	 *  @throws IOException if a read error or end-of-file occurs.
	 */
	public void readFrames( float[][] data, int offset, int length ) throws IOException {
		bh.readFrames( data, offset, length );
		framePosition += length;
	}

	/**
	 *	Writes sample frames to the file starting at the current position.
	 *  If you write past the previous end of the file, the <code>length</code>
	 *  field of the internal <code>SURFFileDescr</code> is updated.
	 *  Since you get a reference from <code>getDescr</code> and not
	 *  a copy, using this reference to the description will automatically
	 *  give you the correct file length.
	 *
	 *  @param  data	buffer holding the frames to write to hard-disc.
	 *					the samples must be de-interleaved such that
	 *					data[0][] holds the first channel, data[1][]
	 *					holds the second channel etc.
	 *  @param  offset  offset in the buffer in sample frames, such
	 *					that he first frame of the first channel will
	 *					be read from data[0][offset] etc.
	 *  @param  length  number of continuous frames to write.
	 *
	 *  @throws IOException if a write error occurs.
	 */
	public void writeFrames( float[][] data, int offset, int length ) throws IOException {
		bh.writeFrames( data, offset, length );
		framePosition += length;

		if( framePosition > afd.length ) {
			if( (framePosition > updateLen) || (System.currentTimeMillis() > updateTime) ) {
				flush();
			} else {
				afd.length = framePosition;
			}
		}
	}
	
	/**
	 *	Returns the number of frames
	 *	in the file.
	 *
	 *	@return	the number of sample frames
	 *			in the file. includes pending
	 *			buffer content
	 *
	 *	@throws	IOException	this is never thrown
	 *			but declared as of the <code>InterleavedStreamFile</code>
	 *			interface
	 */
	public long getFrameNum() throws IOException {
		return afd.length;
	}

	public void setFrameNum( long frame ) throws IOException {
		final long physical	= afh.getSampleDataOffset() + frame * bytesPerFrame;

		raf.setLength( physical );
		if( framePosition > frame ) framePosition = frame;
		afd.length	= frame;
	}

	/**
	 *	Returns the number of channels
	 *	in the file.
	 *
	 *	@return	the number of channels
	 */
	public int getChannelNum() {
		return afd.channels;
	}

	/**
	 *	Truncates the file to the size represented
	 *	by the current file position. The file
	 *	must have been opened in write mode.
	 *	Truncation occurs only if frames exist
	 *	beyond the current file position, which implicates
	 *	that you have set the position using <code>seekFrame</code>
	 *	to a location before the end of the file.
	 *	The header information is immediately updated.
	 *
	 *	@throws	IOException	if truncation fails
	 */
	public void truncate() throws IOException {
		fch.truncate( fch.position() );
		if( framePosition != afd.length ) {
			afd.length	= framePosition;
			updateTime	= System.currentTimeMillis() + 10000;
			afh.updateHeader( afd );
			updateLen	= framePosition + updateStep;
		}
	}

	/**
	 *	Copies sample frames from a source sound file
	 *	to a target file (either another sound file
	 *	or any other class implementing the
	 *	<code>InterleavedStreamFile</code> interface).
	 *	Both files must have the same number of channels.
	 *
	 *	@param	target	to file to copy to from this audio file
	 *	@param	length	the number of frames to copy. Reading
	 *					and writing begins at the current positions
	 *					of both files.
	 *
	 *	@throws	IOException	if a read or write error occurs
	 */
	public void copyFrames( InterleavedStreamFile target, long length ) throws IOException {
		int chunkLength;
		int			tempBufSize	= (int) Math.min( length, 8192 );
		float[][]	tempBuf		= new float[ channels ][ tempBufSize ];
		
		while( length > 0 ) {
			chunkLength	= (int) Math.min( length, tempBufSize );
			this.readFrames( tempBuf, 0, chunkLength );
			target.writeFrames( tempBuf, 0, chunkLength );
			length -= chunkLength;
		}
	}

	/**
	 *  Flushes and closes the file
	 *
	 *  @throws IOException if an error occurs during buffer flush
	 *						or closing the file.
	 */
	public void close() throws IOException {
		if( mode == MODE_READWRITE ) {
			fch.force( true );
			afh.updateHeader( afd );
		}
		raf.close();
	}

	/**
	 *  Flushes and closes the file. As opposed
	 *	to <code>close()</code>, this does not
	 *	throw any exceptions but simply ignores any errors.
	 *
	 *	@see	#close()
	 */
	public void cleanUp() {
		try {
			close();
		}
		catch( IOException e ) { /* ignored */ }
	}
	
	/**
	 *  Reads metadata and comment annotations into the audio file
	 *  description if there are any. This method sets the 
	 *  <code>KEY_METADATA</code> or the <code>KEY_COMMENT</code> 
	 *  property of the afd, if metadata or comment annotations are available
	 *
	 *	@see	#getDescr()
	 *	@see	SURFFileDescr#KEY_COMMENT
	 *  @see	SURFFileDescr#KEY_METADATA
	 *  @see	SURFFileDescr#KEY_NOTES
	 *
	 *	@throws	IOException	if a read or parsing error occurs
	 */
	public void readMetadata() throws IOException {
		afh.readMetadata();
	}

	/**
	 *  Reads markers into the audio file description
	 *  if there are any. This method sets the <code>KEY_MARKERS</code>
	 *  property of the afd, if markers are available. It sets
	 *  the <code>KEY_LOOP</code> property if a loop span is available.
	 *
	 *	@see	#getDescr()
	 *	@see	AudioFileDescr#KEY_MARKERS
	 *	@see	AudioFileDescr#KEY_LOOP
	 *
	 *	@throws	IOException	if a read or parsing error occurs
	 */
	public void readMarkers() throws IOException {
		afh.readMarkers();
	}
	
	// create a method to read all the crap from the file

	protected static final String getResourceString( String key ) {
		return IOUtil.getResourceString( key );
	}
	
	// -------- BufferHandler Classes --------
	
	private abstract class BufferHandler {
		protected BufferHandler() { /* empty */ }
		protected abstract void writeFrames( float[][] frames, int off, int len ) throws IOException;
		protected abstract void readFrames( float[][] frames, int off, int len ) throws IOException;
	}
	
	private class ByteBufferHandler extends BufferHandler {
		private final byte[]	arrayBuf;

		protected ByteBufferHandler() {
			arrayBuf	= new byte[ byteBuf.capacity() ];
		}

		protected void writeFrames( float[][] frames, int offset, int length ) throws IOException {
			int		i, j, m, ch, chunkLength;
			float[]	b;

			while( length > 0 ) {
				chunkLength = Math.min( frameBufCapacity, length );
				m			= chunkLength * bytesPerFrame;
				for( ch = 0; ch < channels; ch++ ) {
					b = frames[ ch ];
					for( i = ch, j = offset; i < m; i += channels, j++ ) {
						arrayBuf[ i ] = (byte) (b[ j ] * 0x7F);
					}
				}
				byteBuf.clear();
				byteBuf.put( arrayBuf, 0, m );
				byteBuf.flip();
				fch.write( byteBuf );
				length -= chunkLength;
				offset += chunkLength;
			}
		}

		protected void readFrames( float[][] frames, int offset, int length ) throws IOException {
			int		i, j, m, ch, chunkLength;
			float[]	b;
		
			while( length > 0 ) {
				chunkLength = Math.min( frameBufCapacity, length );
				m			= chunkLength * bytesPerFrame;
				byteBuf.rewind().limit( m );
				fch.read( byteBuf );
				byteBuf.flip();
				byteBuf.get( arrayBuf, 0, m );
				for( ch = 0; ch < channels; ch++ ) {
					b = frames[ ch ];
					if( b == null ) continue;
					for( i = ch, j = offset; i < m; i += channels, j++ ) {
						b[ j ]	= (float) arrayBuf[ i ] / 0x7F;
					}
				}
				length -= chunkLength;
				offset += chunkLength;
			}
		}
	}

	// float to byte = f*0x7F+0x80 (-1 ... +1 becomes 0x01 to 0xFF)
	// which is how libsndfile behaves
	private class UByteBufferHandler extends BufferHandler {
		private final byte[]	arrayBuf;

		protected UByteBufferHandler()
		{
			arrayBuf	= new byte[ byteBuf.capacity() ];
		}

		protected void writeFrames( float[][] frames, int offset, int length ) throws IOException {
			int		i, j, m, ch, chunkLength;
			float[]	b;

			while( length > 0 ) {
				chunkLength = Math.min( frameBufCapacity, length );
				m			= chunkLength * bytesPerFrame;
				for( ch = 0; ch < channels; ch++ ) {
					b = frames[ ch ];
					for( i = ch, j = offset; i < m; i += channels, j++ ) {
						arrayBuf[ i ] = (byte) (b[ j ] * 0x7F + 0x80);
					}
				}
				byteBuf.clear();
				byteBuf.put( arrayBuf, 0, m );
				byteBuf.flip();
				fch.write( byteBuf );
				length -= chunkLength;
				offset += chunkLength;
			}
		}

		protected void readFrames( float[][] frames, int offset, int length ) throws IOException {
			int		i, j, m, ch, chunkLength;
			float[]	b;
		
			while( length > 0 ) {
				chunkLength = Math.min( frameBufCapacity, length );
				m			= chunkLength * bytesPerFrame;
				byteBuf.rewind().limit( m );
				fch.read( byteBuf );
				byteBuf.flip();
				byteBuf.get( arrayBuf, 0, m );
				for( ch = 0; ch < channels; ch++ ) {
					b = frames[ ch ];
					if( b == null ) continue;
					for( i = ch, j = offset; i < m; i += channels, j++ ) {
						if( arrayBuf[ i ] < 0 ) { // hmmm, java can't handle unsigned bytes
							b[ j ]	= (float) (0x80 + arrayBuf[ i ]) / 0x7F;
						} else {
							b[ j ]	= (float) (arrayBuf[ i ] - 0x80) / 0x7F;
						}
					}
				}
				length -= chunkLength;
				offset += chunkLength;
			}
		}
	}

	private class ShortBufferHandler extends BufferHandler {
		private final ShortBuffer	viewBuf;
		private final short[]		arrayBuf;
	
		protected ShortBufferHandler() {
			byteBuf.clear();
			viewBuf		= byteBuf.asShortBuffer();
			arrayBuf	= new short[ viewBuf.capacity() ];
		}

		protected void writeFrames( float[][] frames, int offset, int length ) throws IOException {
			int		i, j, m, ch, chunkLength;
			float[]	b;

			while( length > 0 ) {
				chunkLength = Math.min( frameBufCapacity, length );
				m			= chunkLength * channels;
				for( ch = 0; ch < channels; ch++ ) {
					b = frames[ ch ];
					if( b == null ) continue;
					for( i = ch, j = offset; i < m; i += channels, j++ ) {
						arrayBuf[ i ] = (short) (b[ j ] * 0x7FFF);
					}
				}
				viewBuf.clear();
				viewBuf.put( arrayBuf, 0, m );
				byteBuf.rewind().limit( chunkLength * bytesPerFrame );
				fch.write( byteBuf );
				length -= chunkLength;
				offset += chunkLength;
			}
		}

		protected void readFrames( float[][] frames, int offset, int length ) throws IOException {
			int		i, j, m, ch, chunkLength;
			float[]	b;
		
			while( length > 0 ) {
				chunkLength = Math.min( frameBufCapacity, length );
				m			= chunkLength * channels;
				byteBuf.rewind().limit( chunkLength * bytesPerFrame );
				fch.read( byteBuf );
				viewBuf.clear();
				viewBuf.get( arrayBuf, 0, m );
				for( ch = 0; ch < channels; ch++ ) {
					b = frames[ ch ];
					if( b == null ) continue;
					for( i = ch, j = offset; i < m; i += channels, j++ ) {
						b[ j ]	= (float) arrayBuf[ i ] / 0x7FFF;
					}
				}
				length -= chunkLength;
				offset += chunkLength;
			}
		}
	}

	/*
	 *  24bit big endian
	 */
	private class ThreeByteBufferHandler extends BufferHandler {
		private final byte[]		arrayBuf;
		private final int			chStep = (channels - 1) * 3;
	
		protected ThreeByteBufferHandler() {
			// note : it's *not* faster to use ByteBuffer.allocate()
			// and ByteBuffer.array() than this implementation
			// (using ByteBuffer.allocateDirect() and bulk get into a separate arrayBuf)
			arrayBuf	= new byte[ byteBuf.capacity() ];
		}

		protected void writeFrames( float[][] frames, int offset, int length ) throws IOException {
			int		i, j, k, m, ch, chunkLength;
			float[]	b;

			while( length > 0 ) {
				chunkLength = Math.min( frameBufCapacity, length );
				m			= chunkLength * bytesPerFrame;
				for( ch = 0; ch < channels; ch++ ) {
					b = frames[ ch ];
					for( i = ch * 3, j = offset; i < m; i += chStep, j++ ) {
						k				= (int)  (b[ j ] * 0x7FFFFF);
						arrayBuf[ i++ ] = (byte) (k >> 16);
						arrayBuf[ i++ ] = (byte) (k >> 8);
						arrayBuf[ i++ ] = (byte)  k;
					}
				}
				byteBuf.clear();
				byteBuf.put( arrayBuf, 0, m );
				byteBuf.flip();
				fch.write( byteBuf );
				length -= chunkLength;
				offset += chunkLength;
			}
		}

		protected void readFrames( float[][] frames, int offset, int length ) throws IOException {
			int		i, j, m, ch, chunkLength;
			float[]	b;
		
			while( length > 0 ) {
				chunkLength = Math.min( frameBufCapacity, length );
				m			= chunkLength * bytesPerFrame;
				byteBuf.rewind().limit( m );
				fch.read( byteBuf );
				byteBuf.flip();
				byteBuf.get( arrayBuf, 0, m );
				for( ch = 0; ch < channels; ch++ ) {
					b = frames[ ch ];
					if( b == null ) continue;
					for( i = ch * 3, j = offset; i < m; i += chStep, j++ ) {
						b[ j ]	= (float) ((arrayBuf[ i++ ] << 16 ) |
										  ((arrayBuf[ i++ ] & 0xFF) << 8) |
										   (arrayBuf[ i++ ] & 0xFF)) / 0x7FFFFF;
					}
				}
				length -= chunkLength;
				offset += chunkLength;
			}
		}
	}

	/*
	 *  24bit little endian
	 */
	private class ThreeLittleByteBufferHandler extends BufferHandler {
		private final byte[]		arrayBuf;
		private final int			chStep = (channels - 1) * 3;
	
		protected ThreeLittleByteBufferHandler() {
			// note : it's *not* faster to use ByteBuffer.allocate()
			// and ByteBuffer.array() than this implementation
			// (using ByteBuffer.allocateDirect() and bulk get into a separate arrayBuf)
			arrayBuf	= new byte[ byteBuf.capacity() ];
		}
		
		protected void writeFrames( float[][] frames, int offset, int length ) throws IOException {
			int		i, j, k, m, ch, chunkLength;
			float[]	b;

			while( length > 0 ) {
				chunkLength = Math.min( frameBufCapacity, length );
				m			= chunkLength * bytesPerFrame;
				for( ch = 0; ch < channels; ch++ ) {
					b = frames[ ch ];
					if( b == null ) continue;
					for( i = ch * 3, j = offset; i < m; i += chStep, j++ ) {
						k				= (int)  (b[ j ] * 0x7FFFFF);
						arrayBuf[ i++ ] = (byte)  k;
						arrayBuf[ i++ ] = (byte) (k >> 8);
						arrayBuf[ i++ ] = (byte) (k >> 16);
					}
				}
				byteBuf.clear();
				byteBuf.put( arrayBuf, 0, m );
				byteBuf.flip();
				fch.write( byteBuf );
				length -= chunkLength;
				offset += chunkLength;
			}
		}

		protected void readFrames( float[][] frames, int offset, int length ) throws IOException {
			int		i, j, m, ch, chunkLength;
			float[]	b;
		
			while( length > 0 ) {
				chunkLength = Math.min( frameBufCapacity, length );
				m			= chunkLength * bytesPerFrame;
				byteBuf.rewind().limit( m );
				fch.read( byteBuf );
				byteBuf.flip();
				byteBuf.get( arrayBuf, 0, m );
				for( ch = 0; ch < channels; ch++ ) {
					b = frames[ ch ];
					if( b == null ) continue;
					for( i = ch * 3, j = offset; i < m; i += chStep, j++ ) {
						b[ j ]	= (float) ((arrayBuf[ i++ ] & 0xFF) |
										  ((arrayBuf[ i++ ] & 0xFF) << 8) |
										   (arrayBuf[ i++ ] << 16 )) / 0x7FFFFF;
					}
				}
				length -= chunkLength;
				offset += chunkLength;
			}
		}
	}

	private class IntBufferHandler extends BufferHandler {
		private final IntBuffer		viewBuf;
		private final int[]			arrayBuf;
	
		protected IntBufferHandler() {
			byteBuf.clear();
			viewBuf		= byteBuf.asIntBuffer();
			arrayBuf	= new int[ viewBuf.capacity() ];
		}

		protected void writeFrames( float[][] frames, int offset, int length ) throws IOException {
			int		i, j, m, ch, chunkLength;
			float[]	b;

			while( length > 0 ) {
				chunkLength = Math.min( frameBufCapacity, length );
				m			= chunkLength * channels;
				for( ch = 0; ch < channels; ch++ ) {
					b = frames[ ch ];
					for( i = ch, j = offset; i < m; i += channels, j++ ) {
						arrayBuf[ i ] = (int) (b[ j ] * 0x7FFFFFFF);
					}
				}
				viewBuf.clear();
				viewBuf.put( arrayBuf, 0, m );
				byteBuf.rewind().limit( chunkLength * bytesPerFrame );
				fch.write( byteBuf );
				length -= chunkLength;
				offset += chunkLength;
			}
		}

		protected void readFrames( float[][] frames, int offset, int length ) throws IOException {
			int		i, j, m, ch, chunkLength;
			float[]	b;
		
			while( length > 0 ) {
				chunkLength = Math.min( frameBufCapacity, length );
				m			= chunkLength * channels;
				byteBuf.rewind().limit( chunkLength * bytesPerFrame );
				fch.read( byteBuf );
				viewBuf.clear();
				viewBuf.get( arrayBuf, 0, m );
				for( ch = 0; ch < channels; ch++ ) {
					b = frames[ ch ];
					if( b == null ) continue;
					for( i = ch, j = offset; i < m; i += channels, j++ ) {
						b[ j ]	= (float) arrayBuf[ i ] / 0x7FFFFFFF;
					}
				}
				length -= chunkLength;
				offset += chunkLength;
			}
		}
	}

	private class FloatBufferHandler extends BufferHandler {
		private final FloatBuffer	viewBuf;
		private final float[]		arrayBuf;
	
		protected FloatBufferHandler() {
			byteBuf.clear();
			viewBuf		= byteBuf.asFloatBuffer();
			arrayBuf	= new float[ viewBuf.capacity() ];
		}

		protected void writeFrames( float[][] frames, int offset, int length ) throws IOException {
			int		i, j, m, ch, chunkLength;
			float[]	b;
		
			while( length > 0 ) {
				chunkLength = Math.min( frameBufCapacity, length );
				m			= chunkLength * channels;
				for( ch = 0; ch < channels; ch++ ) {
					b = frames[ ch ];
					for( i = ch, j = offset; i < m; i += channels, j++ ) {
						arrayBuf[ i ] = b[ j ];
					}
				}
				viewBuf.clear();
				viewBuf.put( arrayBuf, 0, m );
				byteBuf.rewind().limit( chunkLength * bytesPerFrame );
				fch.write( byteBuf );
				length -= chunkLength;
				offset += chunkLength;
			}
		}

		protected void readFrames( float[][] frames, int offset, int length ) throws IOException {
			int		i, j, m, ch, chunkLength;
			float[]	b;
		
			while( length > 0 ) {
				chunkLength = Math.min( frameBufCapacity, length );
				m			= chunkLength * channels;
				byteBuf.rewind().limit( chunkLength * bytesPerFrame );
				fch.read( byteBuf );
				viewBuf.clear();
				viewBuf.get( arrayBuf, 0, m );
				for( ch = 0; ch < channels; ch++ ) {
					b = frames[ ch ];
					if( b == null ) continue;
					for( i = ch, j = offset; i < m; i += channels, j++ ) {
						b[ j ]	= arrayBuf[ i ];
					}
				}
				length -= chunkLength;
				offset += chunkLength;
			}
		}
	}

	private class DoubleBufferHandler extends BufferHandler {
		private final DoubleBuffer	viewBuf;
		private final double[]		arrayBuf;
	
		protected DoubleBufferHandler() {
			byteBuf.clear();
			viewBuf		= byteBuf.asDoubleBuffer();
			arrayBuf	= new double[ viewBuf.capacity() ];
		}

		protected void writeFrames( float[][] frames, int offset, int length ) throws IOException {
			int		i, j, m, ch, chunkLength;
			float[]	b;
		
			while( length > 0 ) {
				chunkLength = Math.min( frameBufCapacity, length );
				m			= chunkLength * channels;
				for( ch = 0; ch < channels; ch++ ) {
					b = frames[ ch ];
					for( i = ch, j = offset; i < m; i += channels, j++ ) {
						arrayBuf[ i ] = b[ j ];
					}
				}
				viewBuf.clear();
				viewBuf.put( arrayBuf, 0, m );
				byteBuf.rewind().limit( chunkLength * bytesPerFrame );
				fch.write( byteBuf );
				length -= chunkLength;
				offset += chunkLength;
			}
		}

		protected void readFrames( float[][] frames, int offset, int length ) throws IOException {
			int		i, j, m, ch, chunkLength;
			float[]	b;
		
			while( length > 0 ) {
				chunkLength = Math.min( frameBufCapacity, length );
				m			= chunkLength * channels;
				byteBuf.rewind().limit( chunkLength * bytesPerFrame );
				fch.read( byteBuf );
				viewBuf.clear();
				viewBuf.get( arrayBuf, 0, m );
				for( ch = 0; ch < channels; ch++ ) {
					b = frames[ ch ];
					if( b == null ) continue;
					for( i = ch, j = offset; i < m; i += channels, j++ ) {
						b[ j ]	= (float) arrayBuf[ i ];
					}
				}
				length -= chunkLength;
				offset += chunkLength;
			}
		}
	}

	// -------- AudioFileHeader Classes --------

	private abstract class AudioFileHeader {
		
		//protected static final long SECONDS_FROM_1904_TO_1970 = 2021253247L;
	
		protected AudioFileHeader() { /* empty */ }
		
		protected abstract void readHeader( SURFFileDescr descr ) throws IOException;
		protected abstract void writeHeader( SURFFileDescr descr ) throws IOException;
		protected abstract void updateHeader( SURFFileDescr descr ) throws IOException;
		protected abstract long getSampleDataOffset();
		protected abstract ByteOrder getByteOrder();

		// WAV might overwrite this
		protected boolean isUnsignedPCM() { return false; }

		protected void readMetadata() throws IOException { /* empty */ }
		
		// WAV and AIFF might overwrite this
		protected void readMarkers() throws IOException { /* empty */ }
		
		protected final int readLittleUShort() throws IOException {
			final int i = raf.readUnsignedShort();
			return( (i >> 8) | ((i & 0xFF) << 8) );
		}

		protected final int readLittleInt() throws IOException {
			final int i = raf.readInt();
			return( ((i >> 24) & 0xFF) | ((i >> 8) & 0xFF00) | ((i << 8) & 0xFF0000) | (i << 24) );
		}

		/*
		protected final float readLittleFloat() throws IOException {
			final int i = raf.readInt();
			return( Float.intBitsToFloat( ((i >> 24) & 0xFF) | ((i >> 8) & 0xFF00) | ((i << 8) & 0xFF0000) | (i << 24) ));
		}

		protected final long readLittleLong() throws IOException {
			final long n = raf.readLong();
			return( ((n >> 56) & 0xFFL) |
					((n >> 40) & 0xFF00L) |
					((n >> 24) & 0xFF0000L) |
					((n >> 8)  & 0xFF000000L) |
					((n << 8)  & 0xFF00000000L) |
					((n << 24) & 0xFF0000000000L) |
					((n << 40) & 0xFF000000000000L) |
					 (n << 56) );
		}*/

		protected final void writeLittleShort( int i ) throws IOException {
			raf.writeShort( (i >> 8) | ((i & 0xFF) << 8) );
		}

		protected final void writeLittleInt( int i ) throws IOException {
			raf.writeInt( ((i >> 24) & 0xFF) | ((i >> 8) & 0xFF00) | ((i << 8) & 0xFF0000) | (i << 24) );
		}
		
		/*
		protected final void writeLittleLong( long n ) throws IOException {
			raf.writeLong( ((n >> 56) & 0xFFL) |
			               ((n >> 40) & 0xFF00L) |
			               ((n >> 24) & 0xFF0000L) |
			               ((n >> 8)  & 0xFF000000L) |
			               ((n << 8)  & 0xFF00000000L) |
			               ((n << 24) & 0xFF0000000000L) |
			               ((n << 40) & 0xFF000000000000L) |
							(n << 56) );
		}
		*/
		
		/*
		protected final String readNullTermString() throws IOException {
			final StringBuffer	buf = new StringBuffer();
			byte				b;
			
			b	= raf.readByte();
			while( b != 0 ) {
				buf.append( (char) b );
				b	= raf.readByte();
			}
			return buf.toString();
		}
		*/
		
		// --- SURF METHODS ---
		
		// get the text size in bytes when enconding is UTF-8
		protected int getSize(String src) {
			int size = 0;
			try {
				size =  src.getBytes("UTF-8").length;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return size;
		}
		
		// get the UTF-8 byte representation of the text
		protected byte[] getBytes(String src) {
			byte[] bytes = new byte[1];		
			try {
				bytes = src.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return bytes;
		}
	
		/*
		protected abstract class DataInputReader {
			protected final DataInput din;
			
			protected DataInputReader( DataInput din ) {
				this.din = din;
			}
			
			public abstract int readInt() throws IOException;
			public abstract float readFloat() throws IOException;
		}
		
		
		protected class LittleDataInputReader extends DataInputReader {
			public LittleDataInputReader( DataInput din ) {
				super( din );
			}

			public int readInt() throws IOException { return readLittleInt(); }
			public float readFloat() throws IOException { return readLittleFloat(); }
		}

		protected class BigDataInputReader extends DataInputReader {
			public BigDataInputReader( DataInput din ) {
				super( din );
			}

			public int readInt() throws IOException { return din.readInt(); }
			public float readFloat() throws IOException { return din.readFloat(); }
		}
		*/
	}

	private abstract class AbstractRIFFHeader extends AudioFileHeader {
		protected static final int ADTL_MAGIC		= 0x6164746C;	// 'adtl'
		protected static final int LABL_MAGIC		= 0x6C61626C;	// 'labl'
		protected static final int LTXT_MAGIC		= 0x6C747874;	// 'ltxt'
		
		protected static final int NOTE_MAGIC		= 0x6E6F7465;	// 'note'

		// ltxt purpose for regions
		protected static final int RGN_MAGIC		= 0x72676E20;	// 'rgn '

		// fmt format-code
		protected static final int FORMAT_PCM		= 0x0001;
		protected static final int FORMAT_FLOAT		= 0x0003;
		protected static final int FORMAT_EXT		= 0xFFFE;
		
		// info chunk
		protected static final int INFO_MAGIC 		= 0x494E464F; 	// 'INFO'
		
		// info chunk inner chunks
		protected static final int IARL_MAGIC 	= 0x4941524C;		// 'IARL' archival location
		protected static final int IART_MAGIC	= 0x49415254;		// 'IART' artist -> dataset creator
		protected static final int ICMS_MAGIC 	= 0x49434D53;		// 'ICMS' commissioner
		protected static final int ICMT_MAGIC 	= 0x49434D54;		// 'ICMT' comments
		protected static final int ICOP_MAGIC	= 0x49434F50;		// 'ICOP' copyright
		protected static final int ICRD_MAGIC	= 0x49435244;		// 'ICRD' creation date
		protected static final int IKEY_MAGIC 	= 0x494B4559;		// 'IKEY' keywords
		protected static final int INAM_MAGIC 	= 0x494E414D;		// 'INAM' subject
		protected static final int IPRD_MAGIC 	= 0x49505244;		// 'IPRD' product -> original propose of the file
		protected static final int ISBJ_MAGIC 	= 0x4953424A;		// 'ISBJ' subject -> contents of the file
		protected static final int ISFT_MAGIC 	= 0x49534654;		// 'ISFT' software -> software package used to create the file
		protected static final int ISRC_MAGIC 	= 0x49535243;		// 'ISRC' source -> original (person / organization) source of the file
		protected static final int ISRF_MAGIC 	= 0x49535246;		// 'ISRF' source form -> original form of material (.ZIP / .TXT / etc)
		
		protected long 		sampleDataOffset;
		protected long		dataLengthOffset;
		protected long		lastUpdateLength	= 0L;
		protected boolean	isFloat				= false;
		protected boolean	unsignedPCM;
				
		protected AbstractRIFFHeader() { /* empty */ }
	}
	
	private class WAVEHeader extends AbstractRIFFHeader {
		private static final int RIFF_MAGIC		= 0x52494646;		// 'RIFF'
		private static final int WAVE_MAGIC		= 0x57415645;		// 'WAVE' (offset 8)

		// chunk identifiers
		private static final int FMT_MAGIC		= 0x666D7420;		// 'fmt '
		private static final int DATA_MAGIC		= 0x64617461;		// 'data'
		private static final int CUE_MAGIC		= 0x63756520;		// 'cue '

		// embedded LIST (peak speak) / list (rest of the universe speak) format
		private static final int LIST_MAGIC		= 0x6C697374;		// 'list'
		private static final int LIST_MAGIC2	= 0x4C495354;		// 'LIST'
		
		// inherited from the AIFF chunks (used to add comment chunks)
		private static final int COMT_MAGIC		= 0x434F4D54;		// 'COMT' comment
		
		// inherited from the AIFF chunks (used to define the annotations chunks)
		private static final int ANNO_MAGIC		= 0x414E4E4F;		// 'ANNO' annotations
		
		// some of our custom FourCC codes
		private static final int META_MAGIC		= 0x4D455441;		// 'META' metadata
		private static final int CNFG_MAGIC		= 0x434E4647;		// 'CNFG' mandatory configuration chunk
		private static final int TMST_MAGIC		= 0x544D5354;		// 'TMSP' dataset initial timestamp
		private static final int TMZN_MAGIC		= 0x544D5A4E;		// 'TMZN' dataset timezone
		private static final int SPRT_MAGIC		= 0x53505254;		// 'SPRT' sampling rate
		private static final int CHCC_MAGIC		= 0x43484343;		// 'CHCC' channel calibration constant
		
		private static final long riffLengthOffset = 4L;
		
		private long		listMagicOff		= 0L;
		private long		listMagicLen		= 0L;
		private long		cueMagicOff			= 0L;
		
		
		private long		annoMagicOff		= 0L;
		private long		annoMagicLen		= 0L;
		private long 		cnfgMagicOff		= 0L;
		private long		cnfgMagicLen		= 0L;
		private long 		infoMagicOff		= 0L;
		private long		infoMagicLen		= 0L;
		
		protected WAVEHeader() { /* empty */ }
		
		protected void readHeader( SURFFileDescr descr ) throws IOException {
			int		i, i1, i2, i3, chunkLen, essentials, magic, dataLen = 0, bpf = 0;
			long	len;

			raf.readInt();		// RIFF
			raf.readInt();
			len	= raf.length() - 8;
			raf.readInt();		// WAVE
			len	   -= 4;
			chunkLen = 0;
			
			for( essentials = 2; (len > 0) && (essentials > 0); ) {
				if( chunkLen != 0 ) raf.seek( raf.getFilePointer() + chunkLen );	// skip to next chunk
			
				magic		= raf.readInt();
				chunkLen	= (readLittleInt() + 1) & 0xFFFFFFFE;
				len		   -= chunkLen + 8;

				switch( magic ) {
				case FMT_MAGIC:
					essentials--;
					i					= readLittleUShort();		// format
					descr.channels		= readLittleUShort();		// # of channels
					i1					= readLittleInt();			// sample rate (integer)
					descr.rate			= i1;
					i2					= readLittleInt();			// bytes per frame and second (=#chan * #bits/8 * rate)
					bpf		= readLittleUShort();		// bytes per frame (=#chan * #bits/8)
					descr.bitsPerSample	= readLittleUShort();		// # of bits per sample
					if( ((descr.bitsPerSample & 0x07) != 0) ||
						((descr.bitsPerSample >> 3) * descr.channels != bpf) ||
						((descr.bitsPerSample >> 3) * descr.channels * i1 != i2) ) {
											
						throw new IOException( getResourceString( "errAudioFileEncoding" ));
					}
					unsignedPCM			= bpf == 1;

					chunkLen -= 16;

					switch( i ) {
					case FORMAT_PCM:
						descr.sampleFormat = SURFFileDescr.FORMAT_INT;
						break;
					case FORMAT_FLOAT:
						descr.sampleFormat = SURFFileDescr.FORMAT_FLOAT;
						break;
					case FORMAT_EXT:
						if( chunkLen < 24 ) throw new IOException( getResourceString( "errAudioFileIncomplete" ));
						i1 = readLittleUShort();	// extension size
						if( i1 < 22 ) throw new IOException( getResourceString( "errAudioFileIncomplete" ));
						i2 = readLittleUShort();	// # valid bits per sample
						raf.readInt();				// channel mask, ignore
						i3 = readLittleUShort();	// GUID first two bytes
						if( (i2 != descr.bitsPerSample) ||
							((i3 != FORMAT_PCM) &&
							(i3 != FORMAT_FLOAT)) ) throw new IOException( getResourceString( "errAudioFileEncoding" ));
						descr.sampleFormat = i3 == FORMAT_PCM ? SURFFileDescr.FORMAT_INT : SURFFileDescr.FORMAT_FLOAT;
						chunkLen -= 10;
						break;
					default:
						throw new IOException( getResourceString( "errAudioFileEncoding" ));
					}
					break;

				case DATA_MAGIC:
					essentials--;
					sampleDataOffset	= raf.getFilePointer();
					dataLen				= chunkLen;
					break;
				
				case CUE_MAGIC:
					cueMagicOff			= raf.getFilePointer();
					break;

				case LIST_MAGIC:
				case LIST_MAGIC2:
					i	= raf.readInt();
					chunkLen -= 4;
					if( i == ADTL_MAGIC ) {
						listMagicOff = raf.getFilePointer();
						listMagicLen = chunkLen;
					} // if( i == ADTL_MAGIC )
					if(i == ANNO_MAGIC) {
						annoMagicOff = raf.getFilePointer();
						annoMagicLen = chunkLen;
						System.out.println("anno: " + annoMagicLen);
					} // if ( i == ANNO_MAGIC )
					if(i == CNFG_MAGIC) {
						cnfgMagicOff = raf.getFilePointer();
						cnfgMagicLen = chunkLen;
						System.out.println("cnfg: " + cnfgMagicLen);
						// make this change the essentials to account for the config chunk
					}
					if( i == INFO_MAGIC ) {
						infoMagicOff = raf.getFilePointer();
						infoMagicLen = chunkLen;
					}
					break;
		
				default:
					break;
				} // switch( magic )
			} // for( essentials = 2; (len > 0) && (essentials > 0); )
			if( essentials > 0 ) throw new IOException( getResourceString( "errAudioFileIncomplete" ));
			
			descr.length	= dataLen / bpf;
		}
		
		@SuppressWarnings("unchecked")
		protected void writeHeader( SURFFileDescr descr ) throws IOException {
			int					i, i1, i2, i3;
			Region				region;
			Marker				marker;
			Annotation			annotation;
			Info		SURF_info;
			List<Marker>		labels, notes;
			List<Region>		regions;
			List<Annotation>	comments, metadata;
			
			long			pos, pos2;
			//Object			o;
			
			// check rate because original wave only supports integer greater or equal to 1
			if(descr.SURF_sample_rate >= 1)
				descr.rate = descr.SURF_sample_rate;
			else
				descr.rate = 1;

			isFloat = descr.sampleFormat == SURFFileDescr.FORMAT_FLOAT;	// floating point requires FACT extension
			raf.writeInt( RIFF_MAGIC );
			raf.writeInt( 0 );				// Laenge ohne RIFF-Header (Dateilaenge minus 8); unknown now
			raf.writeInt( WAVE_MAGIC );

			// fmt Chunk
			raf.writeInt( FMT_MAGIC );
			writeLittleInt( isFloat ? 18 : 16 );	// FORMAT_FLOAT has extension of size 0
			writeLittleShort( isFloat ? FORMAT_FLOAT : FORMAT_PCM );
			writeLittleShort( descr.channels );
			i1 = (int) (descr.rate + 0.5);
			writeLittleInt( i1 );
			i2 = (descr.bitsPerSample >> 3) * descr.channels;
			writeLittleInt( i1 * i2 );
			writeLittleShort( i2 );
			writeLittleShort( descr.bitsPerSample );
			
			if( isFloat ) raf.writeShort( 0 );
			
			// mandatory config chunk
			raf.writeInt(LIST_MAGIC);
			pos	= raf.getFilePointer();
			raf.writeInt(0);
			raf.writeInt(CNFG_MAGIC);
				
			// write initial timestamp
			if(afd.SURF_initial_timestamp == "") {
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				afd.SURF_initial_timestamp = df.format(Calendar.getInstance().getTime());
			}
			i3 = getSize( afd.SURF_initial_timestamp) + 1;
			raf.writeInt( TMST_MAGIC );
			writeLittleInt( i3 );
			raf.writeBytes( afd.SURF_initial_timestamp );
			if( (i3 & 1) == 0 ) raf.writeByte( 0 ); else raf.writeShort( 0 );
			
			// write timezone
			i3 = getSize( afd.SURF_timezone ) + 1;
			raf.writeInt( TMZN_MAGIC );
			writeLittleInt( i3 );
			if(afd.SURF_timezone == "") {
				afd.SURF_timezone = Calendar.getInstance().getTimeZone().getID();
			}
			raf.write( getBytes( afd.SURF_timezone ) );
			if( (i3 & 1) == 0 ) raf.writeByte( 0 ); else raf.writeShort( 0 );
			
			// write sampling rate
			i3 = Float.SIZE / 8; 
			raf.writeInt( SPRT_MAGIC );
			writeLittleInt( i3 );
			raf.writeFloat(afd.SURF_sample_rate);
			System.out.println("i3 sampling rate: " + i3);
			System.out.println(i3 & 1);
				
			// write channel calibration -> CHECK IF LENGTH = NUM CHANNELS!!!!
			i3 = Float.SIZE / 8;
			for(int c = 0; c < afd.channels; c++) {
				raf.writeInt( CHCC_MAGIC );
				writeLittleInt( i3 );
				raf.writeFloat(afd.SURF_channel_calibration[c]);
			}
			
			// update this list size
			pos2 = raf.getFilePointer();
			i	 = (int) (pos2 - pos - 4);
			System.out.println("i config: " + i);
			if( (i & 1) == 1 ) {
				raf.write( 0 );	// padding byte
				pos2++;
			}
			raf.seek( pos );
			writeLittleInt( i );
			raf.seek( pos2 );
			
			// cue Chunk
			labels  	= (List<Marker>) descr.getProperty( SURFFileDescr.KEY_LABELS ); 	// appliance activity  -> LABEL chunks
			regions  	= (List<Region>) descr.getProperty( SURFFileDescr.KEY_REGIONS ); 	// user activities 	 -> LABELED TEXT chunks
			notes  		= (List<Marker>) descr.getProperty( SURFFileDescr.KEY_NOTES ); 		// localized metadata 	 -> NOTE chunks
			comments 	= (List<Annotation>) descr.getProperty(SURFFileDescr.KEY_COMMENTS);	// comments in the annotations chunk
			metadata 	= (List<Annotation>) descr.getProperty(SURFFileDescr.KEY_METADATA);	// metadata in the annotations chunks
			SURF_info 	= (Info) descr.getProperty(SURFFileDescr.KEY_INFO);
			
			if( ((labels != null) && !labels.isEmpty()) 
					|| ((regions != null) && !regions.isEmpty()) 
					|| ((notes != null) && !notes.isEmpty())) {
				if( labels == null ) labels 	= new ArrayList<Marker>();
				if( regions == null ) regions 	= new ArrayList<Region>();
				if( notes == null ) notes 		= new ArrayList<Marker>();
				
				raf.writeInt( CUE_MAGIC );
				i2	= labels.size() + regions.size() + notes.size();
				writeLittleInt( 24 * i2 + 4 );
				writeLittleInt( i2 );
				
				for( i = 0, i1 = 1; i < labels.size(); i++, i1++ ) {
					marker = (Marker) labels.get( i );
					writeLittleInt( i1 );
					writeLittleInt( i1 );
					raf.writeInt( DATA_MAGIC );
					raf.writeLong( 0 );	// ignore dwChunkStart, dwBlockStart
					writeLittleInt( (int) marker.pos );
				}
				
				for( i = 0; i < regions.size(); i++, i1++ ) {
					region = (Region) regions.get( i );
					writeLittleInt( i1 );
					writeLittleInt( i1 );
					raf.writeInt( DATA_MAGIC );
					raf.writeLong( 0 );	// ignore dwChunkStart, dwBlockStart
					writeLittleInt( (int) region.span.getStart() ); // WRITES ACCORDING TO THE POSITION IN THE SPAN
				}
				
				for( i = 0; i < notes.size(); i++, i1++ ) {
					marker = (Marker) notes.get( i );
					writeLittleInt( i1 );
					writeLittleInt( i1 );
					raf.writeInt( DATA_MAGIC );
					raf.writeLong( 0 );	// ignore dwChunkStart, dwBlockStart
					writeLittleInt( (int) marker.pos );
				}
				
				raf.writeInt( LIST_MAGIC );
				pos	= raf.getFilePointer();
				raf.writeInt( 0 );
				raf.writeInt( ADTL_MAGIC );
				
				for( i = 0, i1 = 1; i < labels.size(); i++, i1++ ) {
					marker	= (Marker) labels.get( i );
					i3 			= getSize( marker.name ) + 5;
					raf.writeInt( LABL_MAGIC );
					writeLittleInt( i3 );
					writeLittleInt( i1 );
					raf.write( getBytes( marker.name ) );
					if( (i3 & 1) == 0 ) raf.writeByte( 0 ); else raf.writeShort( 0 );
				}
				
				for( i = 0; i < notes.size(); i++, i1++ ) {
					marker	= (Marker) notes.get( i );
					i3			= getSize( marker.name ) + 5;
					raf.writeInt( NOTE_MAGIC );
					writeLittleInt( i3 );
					writeLittleInt( i1 );
					raf.write( getBytes( marker.name) );
					if( (i3 & 1) == 0 ) raf.writeByte( 0 ); else raf.writeShort( 0 );
				}
				
				for( i = 0; i < regions.size(); i++, i1++ ) {
					region	= (Region) regions.get( i );
					raf.writeInt( LTXT_MAGIC );
					i3		= getSize( region.name ) + 21;
					writeLittleInt( i3 );
					writeLittleInt( i1 );
					writeLittleInt( (int) region.span.stop );
					raf.writeInt( RGN_MAGIC );
					raf.writeLong( 0 );		// wCountry, wLanguage, wDialect, wCodePage
					raf.write( getBytes( region.name ) );
					if( (i3 & 1) == 0 ) raf.writeByte( 0 ); else raf.writeShort( 0 );
				}
				
				// update 'list' chunk size
				pos2 = raf.getFilePointer();
				i	 = (int) (pos2 - pos - 4);			
				if( (i & 1) == 1 ) {
					raf.write( 0 );	// padding byte
					pos2++;
				}
				raf.seek( pos );
				writeLittleInt( i );
				raf.seek( pos2 );
				
			} // if marker or region list not empty
			
			if( ((metadata != null) && !metadata.isEmpty()) 
					|| ((comments != null) && !comments.isEmpty()) ) {
				if( metadata == null ) metadata = new ArrayList<Annotation>();
				if( comments == null ) comments = new ArrayList<Annotation>();
				
				raf.writeInt(LIST_MAGIC);
				pos	= raf.getFilePointer();
				raf.writeInt(0);
				raf.writeInt(ANNO_MAGIC);
				
				// add the metadata chunks				
				for( i = 0, i1 = 1; i < metadata.size(); i++, i1++ ) {
					annotation	= (Annotation) metadata.get( i );
					i3 = getSize( annotation.content ) + 1;
					raf.writeInt(META_MAGIC);
					writeLittleInt( i3 );
					System.out.println("metadata: " + i3);
					System.out.println(i3 & 1);
					raf.write( getBytes( annotation.content ) );
					if( (i3 & 1) == 0 ) raf.writeByte( 0 ); else raf.writeShort( 0 );
				}
				
				for( i = 0, i1 = 1; i < comments.size(); i++, i1++ ) {
					annotation	= (Annotation) comments.get( i );
					// i3		= annotation.content.length() + 1;
					i3 = getSize( annotation.content ) + 1;
					raf.writeInt(COMT_MAGIC);
					writeLittleInt( i3 );
					System.out.println("comment: " + i3);
					System.out.println(i3 & 1);
					raf.write( getBytes( annotation.content ) );
					if( (i3 & 1) == 0 ) raf.writeByte( 0 ); else raf.writeShort( 0 );
				}
				
				// update this list size
				pos2 = raf.getFilePointer();
				i	 = (int) (pos2 - pos - 4);
				if( (i & 1) == 1 ) {
					raf.write( 0 );	// padding byte
					pos2++;
				}
				raf.seek( pos );
				writeLittleInt( i );
				raf.seek( pos2 );				
				
			} // if metadata or comment list not empty
			
			
			// INFO CHUNK - not mandatory
			if( SURF_info != null) {
				raf.writeInt(LIST_MAGIC);
				pos	= raf.getFilePointer();
				raf.writeInt(0);
				raf.writeInt(INFO_MAGIC);
				
				// write archival location
				if(SURF_info.archival_location != "") {
					i3 = getSize(SURF_info.archival_location) + 1;
					raf.writeInt(IARL_MAGIC);
					writeLittleInt(i3);
					raf.write(getBytes(SURF_info.archival_location));
					if( (i3 & 1) == 0 ) raf.writeByte( 0 ); else raf.writeShort( 0 );
				}
				
				// write dataset creator
				if(SURF_info.file_creator != "") {
					i3 = getSize(SURF_info.file_creator) + 1;
					raf.writeInt(IART_MAGIC);
					writeLittleInt(i3);
					raf.write( getBytes( SURF_info.file_creator ) );
					if( (i3 & 1) == 0 ) raf.writeByte( 0 ); else raf.writeShort( 0 );
				}
				
				// write commissioner
				if(SURF_info.commissioner != "") {
					i3 = getSize(SURF_info.commissioner) + 1;
					raf.writeInt(ICMS_MAGIC);
					writeLittleInt(i3);
					raf.write( getBytes( SURF_info.commissioner ) );
					if( (i3 & 1) == 0 ) raf.writeByte( 0 ); else raf.writeShort( 0 );
				}
				
				// write comments
				if(SURF_info.comments != "") {
					i3 = getSize(SURF_info.comments) + 1;
					raf.writeInt(ICMT_MAGIC);
					writeLittleInt(i3);
					raf.write( getBytes( SURF_info.comments ) );
					if( (i3 & 1) == 0 ) raf.writeByte( 0 ); else raf.writeShort( 0 );
				}
				
				// write copyright
				if(SURF_info.copyright != "") {
					i3 = getSize(SURF_info.copyright) + 1;
					raf.writeInt(ICOP_MAGIC);
					writeLittleInt(i3);
					raf.write( getBytes( SURF_info.copyright ) );
					if( (i3 & 1) == 0 ) raf.writeByte( 0 ); else raf.writeShort( 0 );
				}
				
				// write creation date
				if(SURF_info.creation_date != "") {
					i3 = getSize(SURF_info.creation_date) + 1;
					raf.writeInt(ICRD_MAGIC);
					writeLittleInt(i3);
					raf.write( getBytes( SURF_info.creation_date ) );
					if( (i3 & 1) == 0 ) raf.writeByte( 0 ); else raf.writeShort( 0 );
				}
				
				// write keywords
				if(SURF_info.keywords != "") {
					i3 = getSize(SURF_info.keywords) + 1;
					raf.writeInt(IKEY_MAGIC);
					writeLittleInt(i3);
					raf.write( getBytes( SURF_info.keywords ) );
					if( (i3 & 1) == 0 ) raf.writeByte( 0 ); else raf.writeShort( 0 );
				}
				
				// write subject / name
				if(SURF_info.name != "") {
					i3 = getSize(SURF_info.name) + 1;
					raf.writeInt(INAM_MAGIC);
					writeLittleInt(i3);
					raf.write( getBytes( SURF_info.name ) );
					if( (i3 & 1) == 0 ) raf.writeByte( 0 ); else raf.writeShort( 0 );
				}
				
				// write product
				if(SURF_info.product != "") {
					i3 = getSize(SURF_info.product) + 1;
					raf.writeInt(IPRD_MAGIC);
					writeLittleInt(i3);
					raf.write( getBytes( SURF_info.product ) );
					if( (i3 & 1) == 0 ) raf.writeByte( 0 ); else raf.writeShort( 0 );
				}
				
				// write subject
				if(SURF_info.subject != "") {
					i3 = getSize(SURF_info.subject) + 1;
					raf.writeInt(ISBJ_MAGIC);
					writeLittleInt(i3);
					raf.write( getBytes( SURF_info.subject ) );
					if( (i3 & 1) == 0 ) raf.writeByte( 0 ); else raf.writeShort( 0 );
				}
				
				// write software
				if(SURF_info.software != "") {
					i3 = getSize(SURF_info.software) + 1;
					raf.writeInt(ISFT_MAGIC);
					writeLittleInt(i3);
					raf.write( getBytes( SURF_info.software ) );
					if( (i3 & 1) == 0 ) raf.writeByte( 0 ); else raf.writeShort( 0 );
				}
				
				// write source
				if(SURF_info.source != "") {
					i3 = getSize(SURF_info.source) + 1;
					raf.writeInt(ISRC_MAGIC);
					writeLittleInt(i3);
					raf.write( getBytes( SURF_info.source ) );
					if( (i3 & 1) == 0 ) raf.writeByte( 0 ); else raf.writeShort( 0 );
				}
				
				// write source form
				if(SURF_info.source_form != "") {
					i3 = getSize(SURF_info.source_form) + 1;
					raf.writeInt(ISRF_MAGIC);
					writeLittleInt(i3);
					raf.write( getBytes( SURF_info.source_form ) );
					if( (i3 & 1) == 0 ) raf.writeByte( 0 ); else raf.writeShort( 0 );
				}
				
				// update this list size
				pos2 = raf.getFilePointer();
				i	 = (int) (pos2 - pos - 4);
				if( (i & 1) == 1 ) {
					raf.write( 0 );	// padding byte
					pos2++;
				}
				raf.seek( pos );
				writeLittleInt( i );
				raf.seek( pos2 );	
			} // if info chunk not null
			
			
			// data Chunk (Header)
			raf.writeInt( DATA_MAGIC );
			dataLengthOffset = raf.getFilePointer();
			raf.writeInt( 0 );
			sampleDataOffset = raf.getFilePointer();
			
			updateHeader( descr );
		}
		
		protected void updateHeader( SURFFileDescr descr ) throws IOException {
			long oldPos	= raf.getFilePointer();
			long len	= raf.length();
			if( len == lastUpdateLength ) return;
			
			if( len >= riffLengthOffset + 4 ) {
				raf.seek( riffLengthOffset );
				writeLittleInt( (int) (len - 8) );								// RIFF Chunk len
			}
			if( len >= dataLengthOffset + 4 ) {
				raf.seek( dataLengthOffset );
				writeLittleInt( (int) (len - (dataLengthOffset + 4)) );			// data Chunk len
			}
			raf.seek( oldPos );
			lastUpdateLength = len;
		}
		
		protected long getSampleDataOffset() {
			return sampleDataOffset;
		}
		
		protected ByteOrder getByteOrder() {
			return ByteOrder.LITTLE_ENDIAN;
		}

		protected boolean isUnsignedPCM() {
			return unsignedPCM;
		}
		
		protected void readMarkers() throws IOException {
			if( (listMagicOff == 0L) && (annoMagicOff == 0L)) return;
			
			final Map<Integer, Integer>	mapCues			= new HashMap<Integer, Integer>();
			final Map<Integer, Integer>	mapCueLengths	= new HashMap<Integer, Integer>();
			final Map<Integer, String>	mapCueNames		= new HashMap<Integer, String>();
			final Map<Integer, Integer>	mapCueTypes		= new HashMap<Integer, Integer>();
			final long	oldPos			= raf.getFilePointer();
			final List<Marker>	markers, notes;
			final List<Annotation >comments, metadata;
			final List<Region>	regions;
			final Info SURF_info;
			
			int			i, i1, i2, i3, i4, i5;
			Object		o;
			String		str;
			int			type;
			byte[]		strBuf			= null;

			try {
				if( listMagicOff > 0L ) {
					raf.seek( listMagicOff );
					for( long chunkLen = listMagicLen; chunkLen >= 8; ) {
						i	= raf.readInt();				// sub chunk ID
						i1	= readLittleInt();
						i2	= (i1 + 1) & 0xFFFFFFFE;		// sub chunk length
						chunkLen -= 8;
						switch( i ) {
						case LABL_MAGIC:
							i3		  = readLittleInt();	// dwIdentifier
							i1		 -= 4;
							i2	     -= 4;
							chunkLen -= 4;
							if( strBuf == null || strBuf.length < i1 ) {
								strBuf  = new byte[ Math.max( 64, i1 )];
							}
							raf.readFully( strBuf, 0, i1 );	// null-terminated
							mapCueNames.put( new Integer( i3 ), new String( strBuf, 0, i1 - 1 ));
							
							mapCueTypes.put(new Integer( i3 ), new Integer ( LABL_MAGIC ));
							
							chunkLen -= i1;
							i2		 -= i1;
							break;
						case NOTE_MAGIC:
							i3		  = readLittleInt();	// dwIdentifier
							i1		 -= 4;
							i2	     -= 4;
							chunkLen -= 4;
							if( strBuf == null || strBuf.length < i1 ) {
								strBuf  = new byte[ Math.max( 64, i1 )];
							}
							raf.readFully( strBuf, 0, i1 );	// null-terminated
							mapCueNames.put( new Integer( i3 ), new String( strBuf, 0, i1 - 1 ));
							
							mapCueTypes.put(new Integer( i3 ), new Integer ( NOTE_MAGIC ));
							
							chunkLen -= i1;
							i2		 -= i1;
							break;
						case LTXT_MAGIC:
							i3			= readLittleInt();	// dwIdentifier
							i4			= readLittleInt();	// dwSampleLength (= frames)
							i5			= raf.readInt();	// dwPurpose
							raf.readLong();					// skip wCountry, wLanguage, wDialect, wCodePage
							i1			-= 20;
							i2			-= 20;
							chunkLen	-= 20;
							o			 = new Integer( i3 );
							if( (i1 > 0) && !mapCueNames.containsKey( o )) {	// don't overwrite names
								if( strBuf == null || strBuf.length < i1 ) {
									strBuf  = new byte[ Math.max( 64, i1 )];
								}
								raf.readFully( strBuf, 0, i1 );	// null-terminated
								mapCueNames.put( (Integer) o, new String( strBuf, 0, i1 - 1 ));
								
								mapCueTypes.put(new Integer( i3 ), new Integer ( LTXT_MAGIC ));
								
								chunkLen -= i1;
								i2		 -= i1;
							}
							if( (i4 > 0) || (i5 == RGN_MAGIC) ) {
								mapCueLengths.put( (Integer) o, new Integer( i4 ));
							}
							break;
							
						default:
							break;
						}
						if( i2 != 0 ) {
							raf.seek( raf.getFilePointer() + i2 );
							chunkLen -= i2;
						}
					} // while( chunkLen >= 8 )
				}
				
				if( cueMagicOff > 0L ) {
					raf.seek( cueMagicOff );
					i	= readLittleInt();	// num cues
					for( int j = 0; j < i; j++ ) {
						i1	= readLittleInt();	// dwIdentifier
						raf.readInt();			// dwPosition (ignore, we don't use playlist)
						i2	= raf.readInt();	// should be 'data'
						raf.readLong();			// ignore dwChunkStart and dwBlockStart
						i3	= readLittleInt();	// dwSampleOffset (fails for 64bit space)
						if( i2 == DATA_MAGIC ) {
							mapCues.put( new Integer( i1 ), new Integer( i3 ));
						}
					}
				}
	
				// resolve markers and regions
				if( !mapCues.isEmpty() ) {
					markers = new ArrayList<Marker>();
					regions	= new ArrayList<Region>();
					notes 	= new ArrayList<Marker>();
					
					for( Iterator<Integer> iter = mapCues.keySet().iterator(); iter.hasNext(); ) {
						o	= iter.next();
						i	= ((Integer) mapCues.get( o )).intValue();	// start frame
						str	= (String) mapCueNames.get( o );
						type= ((Integer) mapCueTypes.get(o));
						o	= mapCueLengths.get( o );
						
						switch(type) {
						case LABL_MAGIC:
							if( str == null) str = "THIS IS A LABEL CHUNK";
							markers.add(new Marker( i, str));
							break;
						case NOTE_MAGIC:
							if( str == null) str = "THIS IS A NOTE CHUNK";
							notes.add(new Marker( i, str));
							break;
						case LTXT_MAGIC:
							if( str == null ) str = "THIS IS A LTXT CHUNK";
							regions.add( new Region( new Span( i, ((Integer) o).intValue() ), str )); // change here to the original span
							break;
						default:
							break;
						}
		
					}
					if( !markers.isEmpty() ) afd.setProperty( SURFFileDescr.KEY_LABELS, markers );
					if( !regions.isEmpty() ) afd.setProperty( SURFFileDescr.KEY_REGIONS, regions );
					if( !notes.isEmpty() ) afd.setProperty( SURFFileDescr.KEY_NOTES, notes );
				}
				
				// load annotations
				comments = new ArrayList<Annotation>();
				metadata = new ArrayList<Annotation>();
				if( annoMagicOff > 0L ) {
					raf.seek( annoMagicOff );
					for( long chunkLen = annoMagicLen; chunkLen >= 8; ) {
						i	= raf.readInt();		// sub chunk ID
						i1	= readLittleInt();
						i2	= (i1 + 1) & 0xFFFFFFFE;	// sub chunk length
						chunkLen -= 8;
						switch( i ) {
						case COMT_MAGIC:
							if( strBuf == null || strBuf.length < i1 ) {
								strBuf  = new byte[ Math.max( 64, i1 )];
							}
							raf.readFully( strBuf, 0, i1 );	// null-terminated
							comments.add(new Annotation(new String( strBuf, 0, i1 - 1 )));							
							chunkLen -= i1;
							i2		 -= i1;
							break;
						case META_MAGIC:
							if( strBuf == null || strBuf.length < i1 ) {
								strBuf  = new byte[ Math.max( 64, i1 )];
							}
							raf.readFully( strBuf, 0, i1 );	// null-terminated
							metadata.add(new Annotation(new String( strBuf, 0, i1 - 1 )));
							chunkLen -= i1;
							i2		 -= i1;
							break;
						default:
							break;
						}
						if( i2 != 0 ) {
							raf.seek( raf.getFilePointer() + i2 );
							chunkLen -= i2;
						}
					}
				}
				if( !comments.isEmpty() ) afd.setProperty( SURFFileDescr.KEY_COMMENTS, comments );
				if( !metadata.isEmpty() ) afd.setProperty( SURFFileDescr.KEY_METADATA, metadata );
				
				if(cnfgMagicOff > 0) {
					float[] cc = new float[afd.channels];
					int cc_count = 0;
					raf.seek( cnfgMagicOff );
					for( long chunkLen = cnfgMagicLen; chunkLen >= 8; ) {
						i	= raf.readInt();		// sub chunk ID
						i1	= readLittleInt();
						i2	= (i1 + 1) & 0xFFFFFFFE;	// sub chunk length
						chunkLen -= 8;
						switch( i ) {
						case TMST_MAGIC:
							if( strBuf == null || strBuf.length < i1 ) {
								strBuf  = new byte[ Math.max( 64, i1 )];
							}
							raf.readFully( strBuf, 0, i1 );	// null-terminated
							afd.SURF_initial_timestamp = new String( strBuf, 0, i1 - 1 );
							chunkLen -= i1;
							i2		 -= i1;
							break;
						case TMZN_MAGIC:
							if( strBuf == null || strBuf.length < i1 ) {
								strBuf  = new byte[ Math.max( 64, i1 )];
							}
							raf.readFully( strBuf, 0, i1 );	// null-terminated
							afd.SURF_timezone = new String( strBuf, 0, i1 - 1 );
							chunkLen -= i1;
							i2		 -= i1;
							break;
						case SPRT_MAGIC:
							afd.SURF_sample_rate = raf.readFloat();
							chunkLen -= i1;
							i2		 -= i1;
							break;
						case CHCC_MAGIC:
							System.out.println("read cc");
							cc[cc_count] = raf.readFloat();
							chunkLen -= i1;
							i2		 -= i1;
							System.out.println(cc[cc_count]);
							cc_count ++;
							break;
						default:
							break;
						}
						if( i2 != 0 ) {
							raf.seek( raf.getFilePointer() + i2 );
							chunkLen -= i2;
						}
					}
					afd.SURF_channel_calibration = cc;
				}
				
				if(infoMagicOff > 0) {
					raf.seek( infoMagicOff );
					SURF_info = new Info();
					for( long chunkLen = infoMagicLen; chunkLen >= 8; ) {
						i	= raf.readInt();		// sub chunk ID
						i1	= readLittleInt();
						i2	= (i1 + 1) & 0xFFFFFFFE;	// sub chunk length
						chunkLen -= 8;
						System.out.println("FOURCC:" + i);
						System.out.println("FourCC String: " + FourCCtoString(i));
						switch( i ) {						
						case IARL_MAGIC:
							if( strBuf == null || strBuf.length < i1 ) {
								strBuf  = new byte[ Math.max( 64, i1 )];
							}
							raf.readFully( strBuf, 0, i1 );	// null-terminated
							SURF_info.archival_location = new String( strBuf, 0, i1 - 1 );
							System.out.println(SURF_info.archival_location);
							chunkLen -= i1;
							i2		 -= i1;
							break;
						case IART_MAGIC:
							if( strBuf == null || strBuf.length < i1 ) {
								strBuf  = new byte[ Math.max( 64, i1 )];
							}
							raf.readFully( strBuf, 0, i1 );	// null-terminated
							SURF_info.file_creator = new String( strBuf, 0, i1 - 1,"UTF-8" );
							System.out.println(SURF_info.file_creator);
							chunkLen -= i1;
							i2		 -= i1;
							break;
						case ICMS_MAGIC:
							if( strBuf == null || strBuf.length < i1 ) {
								strBuf  = new byte[ Math.max( 64, i1 )];
							}
							raf.readFully( strBuf, 0, i1 );	// null-terminated
							SURF_info.commissioner = new String( strBuf, 0, i1 - 1 );
							System.out.println(SURF_info.commissioner);
							chunkLen -= i1;
							i2		 -= i1;
							break;
						case ICMT_MAGIC:
							if( strBuf == null || strBuf.length < i1 ) {
								strBuf  = new byte[ Math.max( 64, i1 )];
							}
							raf.readFully( strBuf, 0, i1 );	// null-terminated
							SURF_info.comments = new String( strBuf, 0, i1 - 1 );
							System.out.println(SURF_info.comments);
							chunkLen -= i1;
							i2		 -= i1;
							break;
						case ICOP_MAGIC:
							if( strBuf == null || strBuf.length < i1 ) {
								strBuf  = new byte[ Math.max( 64, i1 )];
							}
							raf.readFully( strBuf, 0, i1 );	// null-terminated
							SURF_info.copyright = new String( strBuf, 0, i1 - 1 );
							System.out.println(SURF_info.copyright);
							chunkLen -= i1;
							i2		 -= i1;
							break;
						case ICRD_MAGIC:
							if( strBuf == null || strBuf.length < i1 ) {
								strBuf  = new byte[ Math.max( 64, i1 )];
							}
							raf.readFully( strBuf, 0, i1 );	// null-terminated
							SURF_info.creation_date = new String( strBuf, 0, i1 - 1 );
							System.out.println(SURF_info.creation_date);
							chunkLen -= i1;
							i2		 -= i1;
							break;
						case IKEY_MAGIC:
							if( strBuf == null || strBuf.length < i1 ) {
								strBuf  = new byte[ Math.max( 64, i1 )];
							}
							raf.readFully( strBuf, 0, i1 );	// null-terminated
							SURF_info.keywords = new String( strBuf, 0, i1 - 1 );
							System.out.println(SURF_info.keywords);
							chunkLen -= i1;
							i2		 -= i1;
							break;
						case INAM_MAGIC:
							if( strBuf == null || strBuf.length < i1 ) {
								strBuf  = new byte[ Math.max( 64, i1 )];
							}
							raf.readFully( strBuf, 0, i1 );	// null-terminated
							SURF_info.name = new String( strBuf, 0, i1 - 1 );
							System.out.println(SURF_info.name);
							chunkLen -= i1;
							i2		 -= i1;
							break;
						case IPRD_MAGIC:
							if( strBuf == null || strBuf.length < i1 ) {
								strBuf  = new byte[ Math.max( 64, i1 )];
							}
							raf.readFully( strBuf, 0, i1 );	// null-terminated
							SURF_info.product = new String( strBuf, 0, i1 - 1 );
							System.out.println(SURF_info.product);
							chunkLen -= i1;
							i2		 -= i1;
							break;
						case ISBJ_MAGIC:
							if( strBuf == null || strBuf.length < i1 ) {
								strBuf  = new byte[ Math.max( 64, i1 )];
							}
							raf.readFully( strBuf, 0, i1 );	// null-terminated
							SURF_info.subject = new String( strBuf, 0, i1 - 1 );
							System.out.println(SURF_info.subject);
							chunkLen -= i1;
							i2		 -= i1;
							break;
						case ISFT_MAGIC:
							if( strBuf == null || strBuf.length < i1 ) {
								strBuf  = new byte[ Math.max( 64, i1 )];
							}
							raf.readFully( strBuf, 0, i1 );	// null-terminated
							SURF_info.software = new String( strBuf, 0, i1 - 1 );
							System.out.println(SURF_info.software);
							chunkLen -= i1;
							i2		 -= i1;
							break;
						case ISRC_MAGIC:
							if( strBuf == null || strBuf.length < i1 ) {
								strBuf  = new byte[ Math.max( 64, i1 )];
							}
							raf.readFully( strBuf, 0, i1 );	// null-terminated
							SURF_info.source = new String( strBuf, 0, i1 - 1 );
							System.out.println(SURF_info.source);
							chunkLen -= i1;
							i2		 -= i1;
							break;
						case ISRF_MAGIC:
							if( strBuf == null || strBuf.length < i1 ) {
								strBuf  = new byte[ Math.max( 64, i1 )];
							}
							raf.readFully( strBuf, 0, i1 );	// null-terminated
							SURF_info.source_form = new String( strBuf, 0, i1 - 1 );
							System.out.println(SURF_info.source_form);
							chunkLen -= i1;
							i2		 -= i1;
							break;
						default:
							break;
						}
						if( i2 != 0 ) {
							raf.seek( raf.getFilePointer() + i2 );
							chunkLen -= i2;
						}
					}
					afd.setProperty(SURFFileDescr.KEY_INFO, SURF_info);
				}
			}
			finally {
				raf.seek( oldPos );
			}
		}
		
		public String FourCCtoString(int value) {
			String s = "";
			s += (char) ((value >> 24) & 0xFF);
			s += (char) ((value >> 16) & 0xFF);
			s += (char) ((value >> 8) & 0xFF);
			s += (char) (value & 0xFF);
			return s;
		}
	} // class WAVEHeader
} // class AudioFile

