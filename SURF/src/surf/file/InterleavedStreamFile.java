/*
 *  InterleavedStreamFile.java
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

import java.io.File;
import java.io.IOException;

/**
 *  A <code>RandomAccessFile</code> wrapper class that using 
 *  <code>java.nio</code> and a <code>FloatBuffer</code> to write single or
 *  multichannel 32bit floating point files, which
 *  for instance are used to store trajectory data.
 *  Files are purely data, headerless, where channels
 *  are written interleaved <code>float[channel0][frame0],
 *  float[channel1][frame0], ..., float[channelN][frame0],
 *  float[channel0][frame1], ... <var>etc</var> ... float[channelN][frameM]</code>
 *  <p>
 *  <code>InterleavedStreamFile</code>s are used inside
 *  <code>NondestructiveTrackEditor</code>s
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.26, 08-Jan-06
 */
public interface InterleavedStreamFile
{
	public void close() throws IOException;

	public void truncate() throws IOException;

	public void readFrames( float[][] data, int offset, int length ) throws IOException;

	public void writeFrames( float[][] data, int offset, int length ) throws IOException;

	public void copyFrames( InterleavedStreamFile target, long length ) throws IOException;

	public void seekFrame( long position ) throws IOException;

	public long getFrameNum() throws IOException;

	public void setFrameNum( long n ) throws IOException;

	public int getChannelNum();

	public long getFramePosition() throws IOException;

	public void flush() throws IOException;

	public File getFile();
}