package com.litgh.httprouter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Node {
	protected String path;
	protected boolean wildChild;
	protected NodeType nodeType;
	protected int maxParams;
	protected String indices;
	protected Node[] children;
	protected int priority;
	protected Handler handler;
	
	public Node() {
		this.children = new Node[] {};
		this.path = "";
		this.indices = "";
	}

	public void addRoute(String path, Handler handler) {
		String fullpath = path;
		this.priority++;
		int numParams = countParams(path);

		if (this.path.length() > 0 || this.children.length > 0) {
			walk(this, fullpath, path, numParams, handler);
		} else {// Empty tree
			insertChild(this, numParams, path, fullpath, handler);
			this.nodeType = NodeType.ROOT;
		}

	}

	private void walk(Node n, String fullpath, String path, int numParams, Handler handler) {
		for (;;) {
			if (numParams > n.maxParams) {
				n.maxParams = numParams;
			}
			int i = 0;
			int max = min(path.length(), n.path.length());

			while (i < max && path.charAt(i) == n.path.charAt(i)) {
				i++;
			}

			if (i < n.path.length()) {
				Node child = new Node();
				child.path = n.path.substring(i);
				child.wildChild = n.wildChild;
				child.indices = n.indices;
				child.children = n.children;
				child.handler = n.handler;
				child.priority = n.priority - 1;

				for (int x = 0; x < child.children.length; i++) {
					if (child.children[i].maxParams > child.maxParams) {
						child.maxParams = child.children[i].maxParams;
					}
				}

				n.children = new Node[] { child };
				n.indices = String.valueOf(n.path.charAt(i));
				n.path = path.substring(0, i);
				n.handler = null;
				n.wildChild = false;
			}

			if (i < path.length()) {
				path = path.substring(i);
				if (n.wildChild) {
					n = n.children[0];
					n.priority++;

					if (numParams > n.maxParams) {
						n.maxParams = numParams;
					}
					numParams--;

					if (path.length() >= n.path.length() && n.path.equals(path.substring(0, n.path.length()))) {
						if (n.path.length() >= path.length() || path.substring(0, n.path.length()) == "/") {
							walk(n, fullpath, path, numParams, handler);
							return;
						}
					}

					throw new RuntimeException("path segment '" + path + "' conflicts with existing wildcard '"
							+ n.path + "' in path '" + fullpath + "'");
				}

				char c = path.charAt(0);
				if (n.nodeType == NodeType.PARAM && c == '/' && n.children.length == 1) {
					n = n.children[0];
					n.priority++;
					walk(n, path, fullpath, numParams, handler);
					return;
				}

				for (int x = 0; x < n.indices.length(); x++) {
					if (c == n.indices.charAt(x)) {
						x = n.incrementChildPrio(n, x);
						n = n.children[x];
						walk(n, path, fullpath, numParams, handler);
						return;
					}
				}

				if (c != ':' && c != '*') {
					n.indices += String.valueOf(c);
					Node child = new Node();
					child.maxParams = numParams;

					List<Node> l = new ArrayList<Node>(Arrays.asList(n.children));
					l.add(child);
					Node[] _new = new Node[n.children.length + 1];
					l.toArray(_new);
					n.children = _new;

					n.incrementChildPrio(n, n.indices.length() - 1);
					n = child;
				}
				insertChild(n, numParams, path, fullpath, handler);
				return;
			} else if (i == path.length()) {
				if (n.handler == null) {
					throw new RuntimeException("a handle is already registered for path '" + fullpath + "'");
				}
				n.handler = handler;
			}
			return;
		}
	}

	private void insertChild(Node n, int numParams, String path, String fullpath, Handler handler) {
		int offset = 0;
		for (int i = 0, max = path.length(); numParams > 0; i++) {
			char c = path.charAt(i);
			if (c != ':' && c != '*') {
				continue;
			}

			int end = i + 1;
			while (end < max && path.charAt(end) != '/') {
				switch (path.charAt(end)) {
				case ':':
				case '*':
					throw new RuntimeException("only one wildcard per path segment is allowed, has: '"
							+ path.substring(i) + "' in path '" + fullpath + "'");
				default:
					end++;
				}
			}

			if (n.children.length > 0) {
				throw new RuntimeException("wildcard route '" + path.substring(i, end)
						+ "' conflicts with existing children in path '" + fullpath + "'");
			}

			if (end - i < 2) {
				throw new RuntimeException("wildcards must be named with a non-empty name in path '" + fullpath + "'");
			}

			if (c == ':') {// param
				if (i > 0) {
					n.path = path.substring(offset, i);
					offset = i;
				}

				Node child = new Node();
				child.nodeType = NodeType.PARAM;
				child.maxParams = numParams;

				n.children = new Node[] { child };
				n.wildChild = true;
				n = child;
				n.priority++;
				numParams--;

				// if the path doesn't end with the wildcard, then there
				// will be another non-wildcard subpath starting with '/'
				if (end < max) {
					n.path = path.substring(offset, end);
					offset = end;

					Node chd = new Node();
					chd.maxParams = numParams;
					chd.priority = 1;
					n.children = new Node[] { chd };
					n = chd;

				}
			} else {// catchAll
				if (end != max || numParams > 1) {
					throw new RuntimeException("catch-all routes are only allowed at the end of the path in path '"
							+ fullpath + "'");
				}
				if (n.path.length() > 0 && n.path.charAt(n.path.length() - 1) == '/') {
					throw new RuntimeException(
							"catch-all conflicts with existing handle for the path segment root in path '" + fullpath
									+ "'");
				}

				i--;
				if (path.charAt(i) == '/') {
					throw new RuntimeException("no / before catch-all in path '" + fullpath + "'");
				}

				n.path = path.substring(offset, i);
				Node child = new Node();
				child.wildChild = true;
				child.nodeType = NodeType.CATCHALL;
				child.maxParams = 1;

				n.children = new Node[] { child };
				n.indices = String.valueOf(path.charAt(i));
				n = child;
				n.priority++;

				// second node: node holding the variable
				child = new Node();
				child.path = path.substring(i);
				child.nodeType = NodeType.CATCHALL;
				child.maxParams = 1;
				child.handler = handler;
				child.priority = 1;

				n.children = new Node[] { child };

				return;
			}
		}

		// insert remaining path part and handle to the leaf
		n.path = path.substring(offset);
		n.handler = handler;
	}
	
	// Returns the handle registered with the given path (key). The values of
	// wildcards are saved to a map.
	// If no handle can be found, a TSR (trailing slash redirect) recommendation is
	// made if a handle exists with an extra (without the) trailing slash for the
	// given path.
	public Action getValue(String path) {
		Node n = this;
		List<Param> p = new ArrayList<Param>();
		Boolean tsr = Boolean.FALSE;
		Handler handle = walk(n, path, p, tsr);

		return new Action(handle, p, tsr);
	}

	private Handler walk(Node n, String path, List<Param> p, Boolean tsr) {
		while (true) {
			if (path.length() > n.path.length()) {
				if (path.substring(0, n.path.length()).equals(n.path)) {
					path = path.substring(n.path.length());

					// If this node does not have a wildcard (param or catchAll)
					// child, we can just look up the next child node and
					// continue
					// to walk down the tree
					if (!n.wildChild) {
						char c = path.charAt(0);
						for (int i = 0; i < n.indices.length(); i++) {
							if (c == n.indices.charAt(i)) {
								n = n.children[i];
								return walk(n, path, p, tsr);
							}
						}

						// Nothing found.
						// We can recommend to redirect to the same URL without
						// a
						// trailing slash if a leaf exists for that path.
						tsr = ("/".equals(path) && n.handler != null);
						return null;
					}

					// handle wildcard child
					n = n.children[0];
					if (n.nodeType == NodeType.PARAM) {
						// find param end (either '/' or path end)
						int end = 0;
						while (end < path.length() && path.charAt(end) != '/') {
							end++;
						}

						// save param value
						if (p == null) {
							p = new ArrayList<Param>(n.maxParams);
						}
						int idx = p.size();
						Param param = new Param();
						param.setKey(n.path.substring(1));
						param.setValue(path.substring(0, end));
						p.add(idx, param);

						if (end < path.length()) {
							if (n.children.length > 0) {
								path = path.substring(end);
								n = n.children[0];
								return walk(n, path, p, tsr);
							}

							tsr = path.length() == end + 1;
							return null;
						}

						if (n.handler != null) {
							return n.handler;
						} else if (n.children.length == 1) {
							n = n.children[0];
							tsr = ("/".equals(n.path) && n.handler != null);
							return null;
						}
					} else if (n.nodeType == NodeType.CATCHALL) {
						if (p == null) {
							p = new ArrayList<Param>(n.maxParams);
						}

						int idx = p.size();
						Param param = new Param();
						param.setKey(n.path.substring(2));
						param.setValue(path);
						p.add(idx, param);

						return n.handler;
					} else {
						throw new RuntimeException("invalid node type");
					}
				}
			} else if (path.equals(n.path)) {
				if (n.handler != null) {
					return n.handler;
				}

				if ("/".equals(path) && n.wildChild && n.nodeType != NodeType.ROOT) {
					tsr = Boolean.TRUE;
					return null;
				}

				for (int i = 0; i < n.indices.length(); i++) {
					if (n.indices.charAt(i) == '/') {
						n = n.children[i];
						tsr = (n.path.length() == 1 && n.handler != null)
								|| (n.nodeType == NodeType.CATCHALL && n.children[0].handler != null);
						return null;
					}
				}
				return null;
			}
			tsr = ("/".equals(path))
					|| (n.path.length() == path.length() + 1 && n.path.charAt(path.length()) == '/'
							&& path.equals(n.path.substring(n.path.length() - 1)) && n.handler != null);
			return null;
		}
	}

	private int incrementChildPrio(Node n, int pos) {
		n.children[pos].priority++;
		int prio = n.children[pos].priority;

		int newPos = pos;
		while (newPos > 0 && n.children[newPos - 1].priority < prio) {
			Node tmpN = n.children[newPos - 1];
			n.children[newPos - 1] = n.children[newPos];
			n.children[newPos] = tmpN;
			newPos--;
		}

		if (newPos != pos) {
			n.indices = n.indices.substring(0, newPos) + n.indices.substring(pos, pos + 1)
					+ n.indices.substring(newPos, pos) + n.indices.substring(pos + 1);
		}

		return newPos;
	}

	private int min(int a, int b) {
		return a <= b ? a : b;
	}

	private int countParams(String path) {
		int n = 0;
		for (int i = 0; i < path.length(); i++) {
			if (path.charAt(i) != ':' && path.charAt(i) != '*') {
				continue;
			}
			n++;
		}
		return n;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isWildChild() {
		return wildChild;
	}

	public void setWildChild(boolean wildChild) {
		this.wildChild = wildChild;
	}

	public NodeType getNodeType() {
		return nodeType;
	}

	public void setNodeType(NodeType nodeType) {
		this.nodeType = nodeType;
	}

	public int getMaxParams() {
		return maxParams;
	}

	public void setMaxParams(int maxParams) {
		this.maxParams = maxParams;
	}

	public String getIndices() {
		return indices;
	}

	public void setIndices(String indices) {
		this.indices = indices;
	}

	public Node[] getChildren() {
		return children;
	}

	public void setChildren(Node[] children) {
		this.children = children;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

}
