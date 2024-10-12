package com.ntros.converter;

public interface Converter<D, M> {

    D toDTO(final M model);
    M toModel(final D dto);
}
