package it.polito.tdp.extflightdelays.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {

	private SimpleWeightedGraph <Airport, DefaultWeightedEdge> grafo;
	private ExtFlightDelaysDAO dao;
	private Map<Integer, Airport> idMap;
	private Map<Airport,Airport> visita;
	
	public Model() {
		dao = new ExtFlightDelaysDAO();
		idMap = new HashMap<Integer,Airport>();
		dao.loadAllAirports(idMap);
	}
	
	public void creaGrafo(int x) {
		this.grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		//aggiungo vertici filtrati
		Graphs.addAllVertices(this.grafo, dao.getVertici(idMap, x));
		
		//aggiungo gli archi
		for(Rotta r: dao.getRotte(idMap)) {
			if(grafo.containsVertex(r.getA1()) && grafo.containsVertex(r.getA2())) {
				DefaultWeightedEdge e = this.grafo.getEdge(r.getA1(), r.getA2()); //essendo non orientato mi da arco indipendentemente da ordine vertici
				if(e == null) {
					Graphs.addEdgeWithVertices(this.grafo, r.getA1(), r.getA2(), r.getN());
				}else {
					double pesoVecchio = this.grafo.getEdgeWeight(e);
					double pesoNuovo = pesoVecchio + r.getN();
					this.grafo.setEdgeWeight(e, pesoNuovo);
				}
			}
		}
		
		System.out.println("Grafo creato:");
		System.out.println("# Vertici: "+grafo.vertexSet().size());
		System.out.println("# Archi: "+grafo.edgeSet().size());
	}

	public Set<Airport> getVertici() {
		// TODO Auto-generated method stub
		return this.grafo.vertexSet();
	}
	
	public List<Airport> trovaPercorsi(Airport a1, Airport a2){
		List<Airport> percorso = new LinkedList<>();
		
		BreadthFirstIterator<Airport, DefaultWeightedEdge> it = new BreadthFirstIterator<>(grafo, a1);
		
		visita = new HashMap<>();
		visita.put(a1, null);
		it.addTraversalListener(new TraversalListener<Airport, DefaultWeightedEdge>(){

			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> e) {
				Airport airport1 = grafo.getEdgeSource(e.getEdge());
				Airport airport2 = grafo.getEdgeTarget(e.getEdge());
				
				if(visita.containsKey(airport1) && !visita.containsKey(airport2)) {
					visita.put(airport2, airport1);
				}else if(visita.containsKey(airport2) && !visita.containsKey(airport1)) {
					visita.put(airport1, airport2);
				}
			}

			@Override
			public void vertexTraversed(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void vertexFinished(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		while(it.hasNext()) {
			it.next();
			
		}
		
		if(!visita.containsKey(a1) || !visita.containsKey(a2)) {
			return null;
		}
		
		percorso.add(a2);
		Airport step = a2;
		while(visita.get(step)!= null) {
			step = visita.get(step);
			percorso.add(0,step);
		}
		return percorso;
	}
}
