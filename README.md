# spring_issues_generic_provider

## Problem
Shows issues with spring implementation of Provider&lt;xxx&gt; when many of these are present 
(e.g. Provider&lt;String&gt;) even when using @Named annotations. 

The issue stem from Spring treating using the provided @Bean type (e.g. String) as the primary filter 
and using @Named as a secondary @Qualifier.

In DefaultListableBeanFactory.findAutowireCandidates() Spring builds a list of possible candidates for 
injection by querying each matching bean for it's type. ObjectFactory (which is what Provider is translated to)
expose their generic type as the bean type. In the presence of many Provider of the same type, this list becomes 
quite large. Only after building the possible candidate list by type, Spring looks at the requested injection for 
qualifiers, e.g. the @Named (or @Qualifier which is what @Named is translated to) and filter that list.
That makes sense as @Named (@Qualifier) is optional, although in the presence of many Provider of the same type,
@Named is always used.

***However - Spring doesn't cache that information (requested bean name to resolved type).***
 
This leads to scanning and re-scanning all Providers of the requested type for each execution.
When the Provider supplies a Prototype bean (e.g. "user name" or "request id"), these rescans start to cost. A lot.

Moreover, because of the modularized behavior of Providers and the desire of developers to create small 
and "functional" (as in functional interface) classes, we often see one Provider calling another Provider just to perform
a function, e.g. "FullUserNameProvider" calling "UserNameProvider" and "CustomerProvider" and combines the two results.

In the presence of multiple compound providers the re-scanning achieves quadratic complexity, 
specifically O(c(h-1) + h(1-h))) where h is the longest chain length c is the total amount of beans of that type.

## Workaround
To workaround this issue we need to cache the relation between the name of the @Named injection requests and 
the provider ObjectFactory that satisfies this request.
While in the general case this is impossible, in the case where only a single Provider fits (which is common)
caching proves very useful - essentially reducing the above quadratic scan to "no-scan" (constant time).

Unfortunately Spring findAutowireCandidates() is not easy to cache and significant duplication of logic is required.
See https://github.com/bironran/spring_issues_generic_provider/blob/master/src/main/java/com/rb/springissues/generic_provider_injection/AutoWireFriendlyDefaultListableBeanFactory.java for details


## Web application integration
In web application integration is done by subclassing WebApplicationContext (Xml or Annotation) and overriding
createBeanFactory() to return the caching BeanFactory, e.g.
```java
public class AutoWireFriendlyXmlWebApplicationContext extends XmlWebApplicationContext {
    @Override
    protected DefaultListableBeanFactory createBeanFactory() {
        return new AutoWireFriendlyDefaultListableBeanFactory(getInternalParentBeanFactory());
    }
}
``` 
   
## Sample run without caching
Time taken: 1497ms
```
height: 1, width: 1, complexity: 0
height: 1, width: 2, complexity: 0
height: 1, width: 3, complexity: 0
height: 1, width: 4, complexity: 0
height: 1, width: 5, complexity: 0
height: 2, width: 1, complexity: 1
height: 2, width: 2, complexity: 3
height: 2, width: 3, complexity: 5
height: 2, width: 4, complexity: 7
height: 2, width: 5, complexity: 9
height: 3, width: 1, complexity: 4
height: 3, width: 2, complexity: 10
height: 3, width: 3, complexity: 16
height: 3, width: 4, complexity: 22
height: 3, width: 5, complexity: 28
height: 4, width: 1, complexity: 9
height: 4, width: 2, complexity: 21
height: 4, width: 3, complexity: 33
height: 4, width: 4, complexity: 45
height: 4, width: 5, complexity: 57
height: 5, width: 1, complexity: 16
height: 5, width: 2, complexity: 36
height: 5, width: 3, complexity: 56
height: 5, width: 4, complexity: 76
height: 5, width: 5, complexity: 96
```

## Sample run with caching
Time taken: 411ms
```
height: 1, width: 1, complexity: 0
height: 1, width: 2, complexity: 0
height: 1, width: 3, complexity: 0
height: 1, width: 4, complexity: 0
height: 1, width: 5, complexity: 0
height: 2, width: 1, complexity: 0
height: 2, width: 2, complexity: 0
height: 2, width: 3, complexity: 0
height: 2, width: 4, complexity: 0
height: 2, width: 5, complexity: 0
height: 3, width: 1, complexity: 0
height: 3, width: 2, complexity: 0
height: 3, width: 3, complexity: 0
height: 3, width: 4, complexity: 0
height: 3, width: 5, complexity: 0
height: 4, width: 1, complexity: 0
height: 4, width: 2, complexity: 0
height: 4, width: 3, complexity: 0
height: 4, width: 4, complexity: 0
height: 4, width: 5, complexity: 0
height: 5, width: 1, complexity: 0
height: 5, width: 2, complexity: 0
height: 5, width: 3, complexity: 0
height: 5, width: 4, complexity: 0
height: 5, width: 5, complexity: 0
```
