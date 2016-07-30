/*
  Copyright (C) 2016 Fred Grott(aka shareme GrottWorkShop)

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied. See the License for the specific language
governing permissions and limitations under License.
 */
package com.github.shareme.gwsdaggerinjector.library;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 *
 * This class allows to inject into objects through a base class,
 * so we don't have to repeat injection code everywhere.
 *
 * The performance drawback is about 0.013 ms per injection on a very slow device,
 * which is negligible in most cases.
 *
 * Example:
 * <pre>{@code
 * Component {
 *     void inject(B b);
 * }
 *
 * class A {
 *     void onCreate() {
 *         ComponentReflectionInjector.inject(this);
 *     }
 * }
 *
 * class B extends A {
 *     @Inject MyDependency dependency;
 * }
 *
 * new B().onCreate() // dependency will be injected at this point
 *
 * class C extends B {
 *
 * }
 *
 * new C().onCreate() // dependency will be injected at this point as well
 * }</pre>
 *
 * @param <T> a type of dagger 2 component.
 * Created by fgrott on 7/30/2016.
 */
@SuppressWarnings("unused")
public class ComponentReflectionInjector<T> implements Injector {

  private final Class<T> componentClass;
  private final T component;
  private final HashMap<Class<?>, Method> methods;

  public ComponentReflectionInjector(Class<T> componentClass, T component) {
    this.componentClass = componentClass;
    this.component = component;
    this.methods = getMethods(componentClass);
  }

  public T getComponent() {
    return component;
  }

  @Override
  public void inject(Object target) {

    Class targetClass = target.getClass();
    Method method = methods.get(targetClass);
    while (method == null && targetClass != null) {
      targetClass = targetClass.getSuperclass();
      method = methods.get(targetClass);
    }

    if (method == null)
      throw new RuntimeException(String.format("No %s injecting method exists in %s component", target.getClass(), componentClass));

    try {
      method.invoke(component, target);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static HashMap<Class<?>, Method> getMethods(Class componentClass) {
    HashMap<Class<?>, Method> methods = new HashMap<>();
    for (Method method : componentClass.getMethods()) {
      Class<?>[] params = method.getParameterTypes();
      if (params.length == 1)
        methods.put(params[0], method);
    }
    return methods;
  }

}
