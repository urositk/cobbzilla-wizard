package org.cobbzilla.wizard.filters;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;

import java.util.Collection;
import java.util.Iterator;

import static org.cobbzilla.util.reflect.ReflectionUtil.arrayClass;

public abstract class MultiEntityFilter<T, A extends ApiAccount, P extends ApiProfile> extends EntityFilter<T> {

    protected abstract A getAccount(ContainerRequest request);
    protected abstract P getActiveProfile(ContainerRequest request, A account);
    protected abstract T filterEntity(T thing, A account, P profile);

    @Override protected boolean shouldFilter(ContainerRequest request, ContainerResponse response,
                                             String responseClassName, Class<?> responseClass) {
        return super.shouldFilter(request, response, responseClassName, responseClass)
                || (responseClass.isArray() && arrayClass(getMatchEntityClass()).isAssignableFrom(responseClass) && ((Object[]) response.getEntity()).length > 0)
                || (Collection.class.isAssignableFrom(responseClass) && firstElementIsAssignableFrom(response));
    }

    private boolean firstElementIsAssignableFrom(ContainerResponse response) {
        final Iterator iter = ((Collection) response.getEntity()).iterator();
        return iter.hasNext() && getMatchEntityClass().isAssignableFrom(iter.next().getClass());
    }

    @Override protected ContainerResponse filter(ContainerRequest request, ContainerResponse response, String responseClassName, Class<?> responseClass) {
        final A account = getAccount(request);
        final P profile = getActiveProfile(request, account);
        if (disableFilteringFor(account, profile)) return response;
        return filterByProfile(responseClass, response, account, profile);
    }

    protected boolean disableFilteringFor(A account, P profile) { return false; }

    protected ContainerResponse filterByProfile(Class<?> responseClass, ContainerResponse response, A account, P profile) {
        final Object entity = response.getEntity();
        if (responseClass.isArray()) {
            for (T thing : (T[]) entity) filterEntity(thing, account, profile);

        } else if (Collection.class.isAssignableFrom(responseClass)) {
            for (T thing : ((Collection<T>) entity)) filterEntity(thing, account, profile);

        } else {
            filterEntity(((T) entity), account, profile);
        }
        return response;
    }

}
