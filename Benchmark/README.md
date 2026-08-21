# Running the Benchmarks
Make sure to import this project as a JMH (Java Microbenchmark Harness) project. And make sure to add a strategoxt.jar
as an external library in order to be able to run compiled code.

The benchmarks run compiled Stratego 2 code on a set of inputs. Compile programs separately using the Stratego compiler,
and paste the compiled Java file in Code/src/main/java/org/example/benchmarkcode.

Then, create a JMH class that runs the compiled code on your inputs.

# Creating inputs
To create the inputs for the provided benchmarks, you can use the Python scripts available in the Input folder.
They take command line arguments to specify the size of the input, and then print it out. You can pipe this output into a 
file.

# Creating graphs
In the Results folder, the raw results for the benchmarks used in the thesis have been included. The Python script to
create the graphs has also been included. It will generate a graph from a given input file.

To create the graph providing an overview of the impact of additional traversal time, use 'multi' as the filename input
instead of a filename.