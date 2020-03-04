package vn.eway.common.paging;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Page<E> {
	private int currentPage;
	private int pageSize;
	private long totalPage;
	private long totalElements;
	private List<E> contents;

	Page(List<E> contents, int currentPage, int pageSize, long totalPage, long totalElements) {
		this.currentPage = currentPage;
		this.pageSize = pageSize;
		this.totalPage = totalPage;
		this.totalElements = totalElements;
		this.contents = contents;
	}

	public Page(List<E> contents, PageRequest pageRequest, long totalElements) {
		this(contents, pageRequest.getIndex(), pageRequest.getSize(), totalElements);
	}

	public Page(List<E> contents, int currentPage, int pageSize, long totalElements) {
		this.currentPage = currentPage;
		this.pageSize = pageSize;
		this.totalPage = (totalElements + (pageSize - 1)) / pageSize;
		this.totalElements = totalElements;
		this.contents = contents;
	}

	public <R> Page<R> map(Function<E, R> mapFn) {
		List<R> result = new ArrayList<>(contents.size());
		contents.forEach(e -> result.add(mapFn.apply(e)));
		return new Page<>(result, currentPage, pageSize, totalPage, totalElements);
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public int getPageSize() {
		return pageSize;
	}

	public long getTotalPage() {
		return totalPage;
	}

	public long getTotalElements() {
		return totalElements;
	}

	public List<E> getContents() {
		return contents;
	}
}