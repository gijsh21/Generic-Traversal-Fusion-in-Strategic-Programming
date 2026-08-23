# Generic Traversal Fusion in Strategic Programming
Repository for my MSc Thesis _Generic Traversal Fusion in Strategic Programming_ (Gijs van der Heide, 2026).

- Contains the source code for the Proof-of-Concept implementation of the developed traversal fusion algorithm, operating on a subset of the Stratego language.

- Contains the code used for the benchmarks in the thesis evaluation.

 - Contains the LaTeX source for the thesis itself, as well as a PDF of the thesis.


## Abstract
Traversing and transforming tree-like data structures is a common occurrence in developing compilers, interpreters, and other program transformation or analysis tools. In more complex programs, multiple traversals are often performed on the same tree, leading to potentially unnecessary repeated visits of nodes. Finding opportunities to reduce repeated visits is an optimization called traversal fusion.

Tree traversals are particularly common in the strategic programming paradigm. In this paradigm, the transformations that are performed are separated from the manner in which trees are traversed, enabling simple and scalable control over traversals [1]. We consider traversal fusion as an optimization for strategic programming languages.

We developed an algorithm to perform traversal fusion in strategic programming languages. Our algorithm is based on TreeFuser and Grafter [2, 3], two related algorithms developed in previous work on imperative programs. The algorithm performs dependency analysis on strategic programs, and then attempts to fuse together recursive calls without introducing dependency violations in order to determine legal opportunities for traversal fusion.

We developed a proof-of-concept (POC) implementation of this algorithm to work on a subset of the Stratego programming language. We evaluated this POC on a synthetic benchmark that mimics real-world usages of strategic programming, as well as a best-case scenario benchmark, to assess the viability and benefit of this optimization for strategic programming languages.

In the best-case scenario benchmark, we measure a mean runtime decrease of 48%. In a benchmark simulating rendering a web page by traversing a render tree, the total number of tree node visits is reduced by over 40%. While we measure a similar performance benefit for this benchmark initially, we find that this is caused by code synthesis, unrelated to fusion. There is no measurable additional performance increase when enabling fusion. We determine that this is most likely caused by overhead when fusing more complex programs, or relatively expensive other parts of the program dominating total runtime.

[1] Ralf Lämmel, Eelco Visser, and Joost Visser. The Essence of Strategic Programming. An
inquiry into trans-paradigmatic genericity. 2002.
[2] Laith Sakka, Kirshanthan Sundararajah, and Milind Kulkarni. “TreeFuser: a frame-
work for analyzing and fusing general recursive tree traversals”. In: Proceedings of the
ACM on Programming Languages 1.OOPSLA (2017). DOI: 10.1145/3133900. URL: http:
//doi.acm.org/10.1145/3133900.
[3] Laith Sakka et al. “Sound, fine-grained traversal fusion for heterogeneous trees”. In:
Proceedings of the 40th ACM SIGPLAN Conference on Programming Language Design and
Implementation, PLDI 2019, Phoenix, AZ, USA, June 22-26, 2019. Ed. by Kathryn S. McKin-
ley and Kathleen Fisher. ACM, 2019, pp. 830–844. ISBN: 978-1-4503-6712-7. DOI: 10.1145/
3314221.3314626. URL: https://doi.org/10.1145/3314221.3314626.

In conclusion, we find that traversal fusion is possible in strategic programming, but several factors can reduce real-world performance impact, limiting its viability as an optimization. We make suggestions to further explore this optimization in the context of strategic programming.
