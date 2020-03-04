package vn.eway.common.paging;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PageRequest {
	private int index;
	private int size;

	public PageRequest(int index, int size) {
		this.index = index;
		this.size = size;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	@JsonIgnore
	public int getOffset() {
		return (index - 1) * size;
	}
}