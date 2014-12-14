package com.netflix.eureka2.server.registry;

/**
 * Implement this interface for any data classes that need to express a data source.
 *
 * @author David Liu
 */
public interface Sourced {
    public Source getSource();
}