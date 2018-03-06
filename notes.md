# Områden

* Domain Driven Security

  * Takes ideas from Domain Driven Design to address security concerns.
  * Bounded Context is important

  "Correct data treatment depending on where in the application it is located or travelling to."

* Domain Driven Design

  * Bounded Context
  * Ubiquitous Language Continuous Integration
  * Model-Driven Design
  * Hands-on Modelers
  * Refactoring Towards Deeper Insight

* XSS & SQL Injection

* Taint Tracking

  * Tre steg
    * Identify untrusted inputs, source tainting.
    * Propagate taint.
    * Sink verifying taint is taken care of.

* Domain Modeling

# Uttryck

* Greenfield: Lacks any constraints imposed by prior work
* Brownfield: Constraints imposed by prior work

  * https://stackoverflow.com/questions/1459941/what-are-greenfield-and-brownfield-applications

* Legacy code: Code that is no longe supported, commonly missused for badly written code that are constantly being patched

  * https://www.techopedia.com/definition/25326/legacy-code

* Bounded Context: Boundary that surrounds a particular model. Keeps the knowledge inside the boundary consistent whilst ignoring the noise from the outside world.

  * http://culttt.com/2014/11/19/bounded-contexts-context-maps-domain-driven-design/

* OWASP

  * Open Web Application Security Project
  * Top 10 security vulnerabilities

* Perl Taint Mode, Ruby
  * Perl och Ruby har Dynamic Taint Propagation implementerat.

# Tidigare arbeten

* JONAS STENDAHL, Domain-Driven Security

# Implementation/Utförande alternativ

* Utföra implementation med samt utan DDS
* Gärmföra/testa skillnaderna
* Bör Dynamic Taint Propagation finnas med i båda? Eller ska ett tredje fall där bara Dynamic Taint Propagation används introduseras?

# Referenser

* reference: \parencite{Evans2015}
* cite: \textcite{Evans2015}

# Handledare

* KTH

  * Möte 1

    * Implicit flow, look if DDS covers it.
    * Tydligt problem samt område man önskar svar på.
    * Case att utvärdera området på.

  * Möte 5 Mars
    * What applications to benchmark
      * maybe 10-20
        * Specify programs to test on
          * two in house
        * Specify soa
        * Mention DroidBench
        * SecuriBench
        * Security policy
          * Sources and Sinks
          * Confidentiality and integrity (Important!!!)
            * Give example of flow
        * False positive and negative!!!!
        * How is libraries handled?

* Omegapoint
  * Möte 1
    * Dynamic = at runtime
