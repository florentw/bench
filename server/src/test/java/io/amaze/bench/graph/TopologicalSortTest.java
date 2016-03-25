package io.amaze.bench.graph;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static io.amaze.bench.graph.DirectedGraphTest.genCyclicGraph;
import static io.amaze.bench.graph.DirectedGraphTest.genSimpleDiamondGraph;
import static io.amaze.bench.graph.TopologicalSort.isAcyclic;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

/**
 * Created on 2/20/16
 *
 * @author Florent Weber (florent.weber@gmail.com)
 */
public class TopologicalSortTest {

    private DirectedGraph<String> graph;

    @Before
    public void before() {
        graph = new DirectedGraph<>();
    }

    @Test(expected = java.lang.NullPointerException.class)
    public void sort_null_graph() throws CyclicGraphException {
        TopologicalSort.sort(null);
    }

    @Test
    public void sort_empty_graph() throws CyclicGraphException {
        List<String> orderedLayers = TopologicalSort.sort(graph);
        assertTrue(orderedLayers.isEmpty());
    }

    @Test
    public void single_vertex() throws CyclicGraphException {
        graph.addNodes("a");
        List<String> orderedLayers = TopologicalSort.sort(graph);
        assertThat(orderedLayers.size(), is(1));
        assertTrue(orderedLayers.contains("a"));
    }

    @Test
    public void multiple_vertices() throws CyclicGraphException {
        graph.addNodes("a");
        graph.addNodes("b");
        List<String> orderedLayers = TopologicalSort.sort(graph);
        assertThat(orderedLayers.size(), is(2));
        assertTrue(orderedLayers.contains("a"));
        assertTrue(orderedLayers.contains("b"));
    }

    @Test
    public void simple_directed_acyclic_graph() throws CyclicGraphException {
        graph.addNodes("b");
        graph.addNodes("a");
        graph.addEdge("a", "b");
        List<String> orderedLayers = TopologicalSort.sort(graph);
        assertEquals(2, orderedLayers.size());
        assertTrue(orderedLayers.get(0).equals("a"));
        assertTrue(orderedLayers.get(1).equals("b"));
    }

    @Test
    public void complex_graph() throws CyclicGraphException {
        // node b has two incoming edges

        graph.addNodes("a");
        graph.addNodes("b");
        graph.addNodes("c");
        graph.addNodes("d");
        graph.addNodes("e");
        graph.addNodes("f");
        graph.addNodes("g");
        graph.addNodes("h");
        graph.addEdge("a", "b");
        graph.addEdge("a", "c");
        graph.addEdge("c", "d");
        graph.addEdge("d", "b");
        graph.addEdge("c", "e");
        graph.addEdge("f", "g");

        List<String> orderedLayers = TopologicalSort.sort(graph);
        assertEquals(8, orderedLayers.size());
        assertTrue(orderedLayers.indexOf("a") < orderedLayers.indexOf("b"));
        assertTrue(orderedLayers.indexOf("a") < orderedLayers.indexOf("c"));
        assertTrue(orderedLayers.indexOf("c") < orderedLayers.indexOf("d"));
        assertTrue(orderedLayers.indexOf("c") < orderedLayers.indexOf("e"));
        assertTrue(orderedLayers.indexOf("d") < orderedLayers.indexOf("b"));
        assertTrue(orderedLayers.indexOf("f") < orderedLayers.indexOf("g"));
    }

    @Test(expected = CyclicGraphException.class)
    public void cyclic_graph_sort_fails() throws CyclicGraphException {
        genCyclicGraph(graph);

        TopologicalSort.sort(graph);
    }

    @Test
    public void cyclic_graph_detection() throws CyclicGraphException {
        genCyclicGraph(graph);

        assertThat(isAcyclic(graph), is(false));
    }

    @Test
    public void acyclic_graph_detection() throws CyclicGraphException {
        genSimpleDiamondGraph(graph);

        assertThat(isAcyclic(graph), is(true));
    }
}