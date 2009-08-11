package org.jbox2d.collision;

import org.jbox2d.common.Vec2;
import org.jbox2d.structs.ContactID;

/**
 * Used for computing contact manifolds.
 */
public class ClipVertex{
	public final Vec2 v;
	public final ContactID id;

	public ClipVertex(){
		v = new Vec2();
		id = new ContactID();
	}

	public void set(final ClipVertex cv){
		v.set(cv.v);
		id.set(cv.id);
	}
}