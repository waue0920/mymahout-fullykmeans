package org.nchc.ltu.mahout09;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.mahout.clustering.fuzzykmeans.FuzzyKMeansDriver;
import org.apache.mahout.clustering.fuzzykmeans.SoftCluster;
import org.apache.mahout.clustering.kmeans.Kluster;
import org.apache.mahout.common.distance.CosineDistanceMeasure;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;

public class FuzzyKMeansClusteringTstChi {

	private static Context context;

	public static final double[][] sourcepoints = { 
			{ 1, 1, 2 }, { 2, 1, 2 }, { 1, 2, 2 }, 
			{ 2, 2, 2 }, { 3, 3, 2 }, { 8, 8, 4 }, 
			{ 9, 8, 4 }, { 8, 9, 4 }, { 9, 9, 4 } 
			};

	public static void writePointsToFile(List<Vector> points, String fileName, FileSystem fs, Configuration conf)
			throws IOException {
		Path path = new Path(fileName);
		SequenceFile.Writer writer = new SequenceFile.Writer(fs, conf, path, LongWritable.class, VectorWritable.class);
		long recNum = 0;
		VectorWritable vec = new VectorWritable();
		for (Vector point : points) {
			vec.set(point);
			writer.append(new LongWritable(recNum++), vec);
		}
		writer.close();
		
	}

    public static void writeClusterCenters( List<SoftCluster> softcluster, String cluster_path,  FileSystem fs,Configuration conf)
            throws IOException {
            Path writerPath = new Path(cluster_path + "/part-00000");
     
            final SequenceFile.Writer writer = new SequenceFile.Writer(fs, conf, writerPath, LongWritable.class, SoftCluster.class);
            long recNum = 0;

            for (SoftCluster sf : softcluster ) {

    			writer.append(new LongWritable(recNum++), sf);

            }
     
            writer.close();
        }
    
	public static List<Vector> getPoints(double[][] raw) {
		System.out.println(" check length :  " );
		List<Vector> points = new ArrayList<Vector>();
		for (int i = 0; i < raw.length; i++) {
			double[] fr = raw[i];
			Vector vec = new RandomAccessSparseVector(fr.length);
			System.out.print( i + ":" + fr.length +",\t");
			vec.assign(fr);
			points.add(vec);
		}
		return points;
	}
	public static void create_dir(String dir) {
		File testData = new File(dir);
		if (!testData.exists()) {
			testData.mkdir();
		}
	}

	public static void main(String args[]) throws Exception {


		int k = 3; // total count of  kmeans center point

		File modelFile = null;

		int Rowc = sourcepoints.length;
		System.out.print("show the last point : ");
		for (int i = 0; i < sourcepoints[Rowc - 1].length; i++) {
			System.out.print("points[" + (Rowc - 1) + "][" + i + "] = " + sourcepoints[Rowc - 1][i] + "\t");
		}
		System.out.println();

		List<Vector> sourcevectors = getPoints(sourcepoints);

		create_dir("testdata09");
		create_dir("testdata09/points");


		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(conf);
		
		String inputPoints="testdata09/points/srcpoint";
		writePointsToFile(sourcevectors, inputPoints, fs, conf);

		List<Vector> Tpoints = sourcevectors;
		System.out.println("\tTpoints: " + Tpoints );
		System.out.println("Tpoints.Tpoints.size: " + Tpoints.size());
/*
		Path path = new Path("testdata/clusters/part-00000");
		SequenceFile.Writer writer = new SequenceFile.Writer(fs, conf, path, Text.class, Cluster.class);

		for (int i = 0; i < k; i++) {
			Vector vec = vectors.get(i);
			System.out.println("vectors.get"+i+": " + vec );
			Cluster cluster = new Cluster(vec, i, new EuclideanDistanceMeasure());
			writer.append(new Text(cluster.getIdentifier()), cluster);
		}
		writer.close();
*/

		List<Vector> random_point = RandomPointsUtil.chooseRandomPoints(Tpoints, k);
		List<SoftCluster> soft_clusters = new ArrayList<SoftCluster>();
		
		System.out.println("\trandomPoints.size(): " + random_point.size());
		System.out.println("\trandomPoints: " + random_point);
		int clusterId = 0;
		for (Vector v : random_point) {
			soft_clusters.add(new SoftCluster(v, clusterId++, new CosineDistanceMeasure()));
		}
		
		String init_center_cluster_dir = "testdata09/scluster";
		writeClusterCenters(soft_clusters, init_center_cluster_dir,fs,conf);
		
		// List<List<SoftCluster>> finalClusters =
		// FuzzyKMeansClusterer.clusterPoints(points, clusters,
		/*
		points - the input List of points
		clusters - the initial List of clusters
		measure - the DistanceMeasure to use
		threshold - the double convergence threshold
		m - the double "fuzzyness" argument (>1)
		numIter - the maximum number of iterations
		
		org.apache.mahout.clustering.fuzzykmeans.FuzzyKMeansClusterer.clusterPoints
		(Iterable<Vector> points, List<SoftCluster> 
		clusters, DistanceMeasure measure, double threshold, double m, int numIter)
		 */
//		List<List<SoftCluster>> finalClusters = FuzzyKMeansClusterer.clusterPoints(Tpoints, clusters,
//				new CosineDistanceMeasure(), 0.01, 3, 10);

		
//		/*
//		 * public static void More ...run(
//						   Path input,
//                         Path clustersIn,
//                         Path output,
//                         double convergenceDelta,
//                         int maxIterations,
//                         float m,
//                         boolean runClustering,
//                         boolean emitMostLikely,
//                         double threshold,
//						   boolean runSequential)
//		 */
		Path fkmeansResult = new Path("./testdata09/fkmeansresult/");
		FuzzyKMeansDriver.run(new Path(inputPoints), new Path(init_center_cluster_dir), fkmeansResult,0.1, 10, 3, true, true, 0.01,true);

		/*
		
		List<List<Double>> distancesList = new ArrayList<List<Double>>();
		FuzzyKMeansClusterer fuzzyKMeansClusterer = new FuzzyKMeansClusterer();
		List<Vector> dataPi = new ArrayList<Vector>();
		for (SoftCluster cluster : finalClusters.get(finalClusters.size() - 1)) {
			List<Double> distances = new ArrayList<Double>();
			System.out.println("Cluster id: " + cluster.getId() + " center: " + cluster.getCenter().asFormatString());

			for (int i = 0; i < Tpoints.size(); i++) {
				distances.add(cluster.getMeasure().distance(Tpoints.get(i), cluster.getCenter()));
				System.out.println("\tcluster.getMeasure().distance(Tpoints.get(i),cluster.getCenter()): "
						+ cluster.getMeasure().distance(Tpoints.get(i), cluster.getCenter()));
				System.out.println("\tdistances: " + distances);
			}


			dataPi.add(fuzzyKMeansClusterer.computePi(clusters, distances));
			distancesList.add(distances);
			
			// computePi = using computeProbWeight to compute the possibility that the point belong to which cluster
			System.out.println(
					"\tfuzzyKMeansClusterer.computePi: " + fuzzyKMeansClusterer.computePi(clusters, distances));

		}

		System.out.println("\tdataPi: " + dataPi);
		System.out.println("\tdistancesList: " + distancesList);
*/

	}

}
