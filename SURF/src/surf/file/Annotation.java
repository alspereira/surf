/*
 *  Annotation.java
 *  
 *
 */

package surf.file;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 *  A struct class: marker in
 *  an audio file. (copied from FScape).
 *
 *  @author		Hanns Holger Rutz
 *  @version	0.23, 25-Feb-08
 *
 *  @see	de.sciss.io.AudioFileDescr#KEY_MARKERS
 */
public class Annotation implements Cloneable, Comparable<Object>, Serializable {

	private static final long serialVersionUID = 1259571589708158936L;

	// -------- public class fields --------
	/**
	 *	A <code>Comparator</code> which can be
	 *	used to sort a list of annotations according
	 *	to the annotations' names.
	 *
	 *	@see	java.util.Collections#sort( List )
	 *	@see	java.util.Collections#sort( List, Comparator )
	 */
	public static final Comparator<Object>	nameComparator	= new NameComparator();

// -------- public instance fields --------	

	
	/**
	 *  A annotation's text content
	 */
	public final String	content;
	
	/**
	 *  type value : annotation type
	 */
	public static final int TYPE_UNKNOWN	= -1;
	
	// maybe we should remove this from here! it's actually a chunk, not a marker by itself
	
	public static final int TYPE_COMMENT 	= 0;
	
	public static final int TYPE_METADATA 	= 1;
	
	
	// -------- public methods --------

	/**
	 *  Constructs a new immutable annotation
	 *
	 *  @param  content	annotation's text content
	 */
	public Annotation( String content) {
		this.content = content;
	}
	
	/**
	 *  Constructs a new immutable marker
	 *  identical to a given marker.
	 *
	 *  @param  orig	the marker to copy
	 */
	public Annotation( Annotation orig ) {
		this.content = orig.content;
	}		

	/**
	 *	Returns a new marker which is
	 *	equal to this one. <code>CloneNotSupportedException</code>
	 *	is never thrown.
	 *
	 *	@return		a new marker with the same name and position
	 *				as this marker
	 */
	public Object clone() throws CloneNotSupportedException {
		return super.clone();	// field by field copy
	}
	
	public int hashCode() {
		return( content.hashCode() );
	}
	
	public boolean equals( Object o ) {
		if( (o != null) && (o instanceof Annotation) ) {
			final Annotation m = (Annotation) o;
			return( this.content.equals( m.content ) );
		} else {
			return false;
		}
	}
	
	/**
	 *	Implementation of the <code>Comparable</code> interface.
	 *	The passed object can be either another <code>Marker</code>
	 *	or a <code>Region</code>. In the latter case, the region's
	 *	start position is compared to this marker's position.
	 *
	 *	@param	o					the object to compare to this marker
	 *	@return						negative or positive value, if the
	 *								object is greater or smaller compared to
	 *								this marker, zero if they are equal
	 *
	 *	@throws	ClassCastException	if <code>o</code> is neither a <code>Markers</code>
	 *								nor a <code>Region</code>
	 */
	public int compareTo( Object o ) {
		return 0;
	}
	
	/**
	 *	Adds marker chronologically to
	 *  a pre-sorted list.
	 *
	 *  @param  annotations		a non-sorted annotation list
	 *  @param  annotation		the annotation to insert
	 *						
	 *	@return	marker index in vector at which it was inserted
	 */
	public static int add( List<Annotation> annotations, Annotation annotation ) {
		annotations.add(annotation);
		return annotations.size() - 1;
	}

	/**
	 *	Gets the index for specific marker in a list.
	 *	Note that if the markers have distinct names (no duplicates), it
	 *	may be more convenient to create a list copy,
	 *	sort it using the <code>nameComparator</code>,
	 *	and looking it up using <code>Collections.binarySearch()</code>.
	 *
	 *  @param  markers		a <code>List</code> whose elements are
	 *						instanceof <code>Marker</code>.
	 *	@param	name		marker name to find
	 *	@param	startIndex	where to begin
	 *	@return				The list index of the first occurrence (beginning
	 *						at <code>startIndex</code>) of a marker whose name equals
	 *						the given name.
	 *
	 *	@see	#nameComparator
	 *	@see	java.util.Collections#binarySearch( List, Object, Comparator )
	 */	
	public static int find( List<Annotation> annotations, String content, int startIndex ) {
		for( int i = startIndex; i < annotations.size(); i++ ) {
			if( ((Annotation) annotations.get( i )).content.equals( content )) 
				return i;
		}
		return -1;
	}
	
// ----------------------- internal classes -----------------------

	private static class NameComparator implements Comparator<Object> {
		protected NameComparator() { /* empty */ }
		
		public int compare( Object o1, Object o2 ) {
			if( o1 instanceof String ) {
				if( o2 instanceof String ) {
					return( ((Comparable<String>) (String) o1).compareTo( (String) o2 ));
				} else if( o2 instanceof Annotation ) {
					return( ((Comparable<String>) (String) o1).compareTo( ((Annotation) o2).content ));
				} else if( o1 instanceof Annotation ) {
					if( o2 instanceof String ) {
						return( ((Comparable<String>) ((Annotation) o1).content).compareTo( (String) o2 ));
					} else if( o2 instanceof Annotation ) {
						return( ((Annotation) o1).content.compareTo( ((Annotation) o2).content ));
					}
				}
			}
			throw new ClassCastException();
		}
				   
		public boolean equals( Object o ) {
			return( (o != null) && (o instanceof NameComparator) );
		}
	}
} // class Marker