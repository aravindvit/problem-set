package graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class BuildSystem {

	Map<String, Vertex> vertexMap = new HashMap<String, Vertex>();
	
	
	static class Vertex {
		public String name;
		public boolean isInstalled;
		List<Vertex> prevLevels = new ArrayList<>();
		List<Vertex> nextLevels = new ArrayList<>();
		HashSet<String> dependsOn = new HashSet<>();
		public Vertex(String name) {
			this.name = name;
		}
		
		/*@Override
		public boolean equals(Object obj) {
			if(obj instanceof Vertex) {
				return this.name.equals( ((Vertex)obj).name );
			} else 
				return false;
		}*/
	}
	
	static class Edge {
		public Vertex src;
		public Vertex dest;
		public Edge(Vertex src, Vertex dest) {
			this.src = src; this.dest = dest;			
		}
	}
	
	public void addDepend(List<String> depends) {
		if(depends == null || depends.size() == 0)
			return;
		
		if(depends.size() == 1) {
			addNodes(depends.get(0), null, null);
			return;
		}
		
		for(int i = 1; i < depends.size(); i++) {
			addNodes(depends.get(i-1), depends.get(i), depends.get(0));
		}
	}
	
	public void addNodes(String srcName, String destName, String dependsOn) {
		if(srcName == null)
			return;
		
		Vertex srcNode = vertexMap.get(srcName);
		if(srcNode == null) {
			srcNode = new Vertex(srcName);
			vertexMap.put(srcName, srcNode);
		}
		
		if(destName == null) {
			return;
		}
		
		Vertex destNode = vertexMap.get(destName);
		if(destNode == null) {
			destNode = new Vertex(destName);
			vertexMap.put(destName, destNode);
			
			//add connections
			addConnection(srcNode, destNode, dependsOn);
			
		} else { 
			//destNode already exits
			//TODO: verify src Node already have connection to destNode if so what to do?
			
			//Verify srcNode has destNode as parent
			if( findParent(srcNode, null, destNode) ) {
				System.out.println( destName + " depends on " + srcName + ", ignoring command" );
			} else {
				addConnection(srcNode, destNode, dependsOn);
			}
		}
	}
	
	public void addConnection(Vertex srcNode, Vertex destNode, String dependsOn){
		//add connections
		srcNode.nextLevels.add(destNode);
		destNode.prevLevels.add(srcNode);
		destNode.dependsOn.add(dependsOn);
	}
	
	public boolean findParent(Vertex node, HashSet<Vertex> visited, Vertex parent) {
		if(visited == null)
			visited = new HashSet<>();
		
		if(visited.contains(node))
			return false;
		
		visited.add(node);
		
		for(Vertex prev : node.prevLevels) {
			if(visited.contains(prev)) 
				continue;
			
			if(prev.equals(parent))
				return true;
			
			findParent(prev, visited, parent);
		}
		
		return false;
	}
	
	public void install(String installName) {
		if(installName == null) 
			return;
		
		Vertex installNode = vertexMap.get(installName);
		if(installNode == null) {
			installNode = new Vertex(installName);
			installNode.isInstalled = true;
			vertexMap.put(installName, installNode);	
			System.out.println("Installing " + installName);
			return;
		}
		
		if(installNode.isInstalled) {
			System.out.println(installName + " Already installed!");
			return;
		}
		
		installPkg(installNode, null, installName);
	}
	
	public void installPkg(Vertex node, HashSet<Vertex> visited, String installName) {
		if(visited == null)
			visited = new HashSet<>();
		
		if(visited.contains(node))
			return;
		
		visited.add(node);
		
		for(Vertex childNodes : node.nextLevels) {
			if(visited.contains(childNodes))
				continue;
			
			installPkg(childNodes, visited, installName);				
		}
		
		if(!node.isInstalled && 
				(node.dependsOn.contains(installName) || node.name.equals(installName))) {
			node.isInstalled = true;
			System.out.println("Installing " + node.name);
		}
	}
	
	public void remove(String removeName) {
		
		Vertex removeNode = vertexMap.get(removeName);
		if(removeNode == null) {
			System.out.println(removeName + " is not installed.");
			return;
		}
		
		Deque<Vertex> deque = new ArrayDeque<>();
		deque.add(removeNode);
		int size = deque.size(); 
		boolean start = true;
		while(!deque.isEmpty()) {
			Vertex curVertex = deque.pollFirst();
			size--;
			if(curVertex.prevLevels == null || curVertex.prevLevels.size() == 0) {
				vertexMap.remove(curVertex);
				System.out.println("Removing " + curVertex.name);
				//remove parent node from all its childNodes
				for(Vertex childNode : curVertex.nextLevels) {
					childNode.prevLevels.remove(curVertex);
					childNode.dependsOn.remove(curVertex.name);
					if(!deque.contains(childNode))
						deque.addLast(childNode);
				}
				
			} else {
				if(start)
					System.out.println(curVertex.name + " is still needed");
			}
			
			start = false;
			
			if(size == 0) {
				size = deque.size();
			}
		}
	}
	
	public void list() {
		Deque<Vertex> stack = new ArrayDeque<>();
		HashSet<Vertex> visited = new HashSet<>();
		for(Vertex node : vertexMap.values()) {
			getList(node, visited, stack);
		}
		Iterator<Vertex> it = stack.iterator();
		while(it.hasNext()) {
			Vertex node = it.next();
			if(node.isInstalled)
				System.out.println(node.name);
		}
		System.out.println();
	}
	
	public void getList(Vertex node, HashSet<Vertex> visited, Deque<Vertex> stack){
		if(visited.contains(node))
			return;
		
		visited.add(node);
		for(Vertex childNode : node.nextLevels) {
			if(visited.contains(childNode))
				continue;
			getList(childNode, visited, stack);
		}
		
		stack.addLast(node);
	}
	
	
	public static void main(String[] args) {
		BuildSystem buildSystem = new BuildSystem();
		Scanner scanner = new Scanner(System.in);
		while(true) {
			String line = scanner.nextLine();
			
			if(line.length() > 80) {
				System.out.println("Line exceeds maximum character limit of 80" );
				continue;
			}
				
			line = line.trim();
			String[] split = line.split(" ");
			String cmd = split[0]; 
			ArrayList<String> params = new ArrayList<>();
			
			boolean invalidParam = false;
			for(int i = 1; i < split.length; i++) {
				if(split[i].trim().length() > 0) {
					String param = split[i].trim();
					if(param.length() > 20) {
						System.out.println(param + " Parameter length is greater than 40");
						invalidParam = true;
						break;
					}
					params.add(split[i].trim());
				}
			}
			if(invalidParam)
				continue;
			
			if(cmd.equals("DEPEND")) {
				buildSystem.addDepend(params);
				
			} else if(cmd.equals("INSTALL")) {
				buildSystem.install(params.get(0));
				
			} else if(cmd.equals("REMOVE")) {
				buildSystem.remove(params.get(0));
				
			} else if(cmd.equals("LIST")) {
				System.out.println("----------------");
				buildSystem.list();
				System.out.println("----------------");
				
			} else {
				System.out.println("Invalid Command");
			}
		}
	}
	
}
