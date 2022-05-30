package org.richfaces.demo.gmap;

public class Place {
	private String pic;
	private String id;
	private String lat;
	private String lng;
	private int zoom;
	private String desc;
	

	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public Place(String id, String pic, String lat, String lng, int zoom, String desc) {
		super();
		this.id = id;
		this.pic = pic;
		this.lat = lat;
		this.lng = lng;
		this.zoom = zoom;
		this.desc = desc;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLat() {
		return lat;
	}
	public void setLat(String lat) {
		this.lat = lat;
	}
	public String getLng() {
		return lng;
	}
	public void setLng(String lng) {
		this.lng = lng;
	}
	public int getZoom() {
		return zoom;
	}
	public void setZoom(int zoom) {
		this.zoom = zoom;
	}
	public String getPic() {
		return pic;
	}
	public void setPic(String pic) {
		this.pic = pic;
	}
}

