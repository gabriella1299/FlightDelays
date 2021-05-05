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
	
	private SimpleWeightedGraph<Airport,DefaultWeightedEdge> grafo;
	private ExtFlightDelaysDAO dao;
	private Map<Integer,Airport> idMap;
	private Map<Airport,Airport> visita;
	
	public Model() {
		this.dao=new ExtFlightDelaysDAO();
		this.idMap=new HashMap<Integer,Airport>();
		this.dao.loadAllAirports(idMap);
	}
	
	public void creaGrafo(int x) {
		//ogni volta che l'utente clicca sul bottone dell'interfaccia crea un nuvo grafo pulito
		this.grafo=new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		//Graphs.addAllVertices(grafo, idMap.values());-->non possiamo farlo perche' abbiamo un filtro sugli aeroporti!
		//Aggiungo vertici 'filtrati'
		Graphs.addAllVertices(this.grafo, dao.getVertici(x,idMap));
		
		//Aggiungo gli archi
		//approccio 1: puo' diventare fattibile con x bassi
		//Usiamo approccio 3, come fatto in LAB08
		for(Rotta r:dao.getRotte(idMap)) { //getRotte ritorna tutte le rotte nel db, ma non sappiamo se siano nel grafo-->controllo!
			//considero la rotta solo se il grafo la contiene
			if(this.grafo.containsVertex(r.getA1()) && this.grafo.containsVertex(r.getA2())) {
				DefaultWeightedEdge e=this.grafo.getEdge(r.getA1(), r.getA2()); //non orientato, ritorna arco se tra i due c'e' indipendentemente dall'ordine
				if(e==null) {
					Graphs.addEdgeWithVertices(grafo, r.getA1(), r.getA2(),r.getN());
				}
				else {
					double pesoVecchio=this.grafo.getEdgeWeight(e);
					double pesoNuovo=pesoVecchio+r.getN();
					this.grafo.setEdgeWeight(e, pesoNuovo);
				}
			}
		}
	}

	public Set<Airport> getVertici() {
		if(grafo != null)
			return grafo.vertexSet();

		return null;
	}
	
	public List<Airport> trovaPercorso(Airport a1,Airport a2){
		List<Airport> percorso=new LinkedList<>();//se size 0 i due aeroporti non sono collegati
		
		BreadthFirstIterator<Airport,DefaultWeightedEdge> it=new BreadthFirstIterator<>(grafo,a1);
		
		visita=new HashMap<>();//ci salviamo l'albero di visita
		visita.put(a1, null);//nodo di partenza, radice e' a1 perche il padre e' null
		
		it.addTraversalListener(new TraversalListener<Airport,DefaultWeightedEdge>(){
			
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
				//Il grafo non e' orientato quindi non c'e' una sorgente e destinazione
				Airport airport1=grafo.getEdgeSource(e.getEdge());
				Airport airport2=grafo.getEdgeTarget(e.getEdge());
				
				if(visita.containsKey(airport1) && !visita.containsKey(airport2)) {
					visita.put(airport2, airport1);//a1 lo conoscevo gia, a1 e' il padre di a2
				}
				else if (visita.containsKey(airport2) && !visita.containsKey(airport1)) {
					visita.put(airport1, airport2);//a2 lo conoscevo gia, a2 e' il padre di a1
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
		
		//VISITA DEL GRAFO
		while(it.hasNext()) {
			it.next();
			//non ci basta visitare, dobbiamo trovare un percorso --> traversalListener
			
		}
		//ottengo il percorso dall'albero di visita

		//se uno dei due aeroporti non è presente nell'albero di visita
		//   -> non c'è nessun percorso
		if(!visita.containsKey(a1) || !visita.containsKey(a2)) {
			return null;//non ci puo' essere un percorso
		}
		
		//altrimenti, parto dal fondo e "risalgo" l'albero
		percorso.add(a2);
		
		Airport step=a2;
		
		while(visita.get(step)!=null) {
			step=visita.get(step);
			percorso.add(0,step);
		}
		return percorso;
	}
	
	//trova percorso con getParent
	public List<Airport> trovaPercorso2(Airport a1,Airport a2){
		
		List<Airport> result=new LinkedList<>();
		
		BreadthFirstIterator<Airport,DefaultWeightedEdge> it=new BreadthFirstIterator<>(grafo,a1);
				
		while(it.hasNext()) {
			it.next();
		}
		
		Airport a=a2;
		
		while(a!=null) {
			result.add(a);
			a=it.getParent(a);
		}
		return result;
	}
	
	public int getNVertici() {
		if(grafo != null)
			return grafo.vertexSet().size();

		return 0;
	}

	public int getNArchi() {
		if(grafo != null)
			return grafo.edgeSet().size();

		return 0;
	}
	
	
}
