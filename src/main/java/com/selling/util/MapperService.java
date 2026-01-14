package com.selling.util;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class MapperService {
  private final ModelMapper modelMapper;

  public MapperService(ModelMapper modelMapper) {
    this.modelMapper = modelMapper;
  }

  public <S, D> D map(S source, Class<D> destType) {
    Object data = source == null ? null : modelMapper.map(source, destType);
    return (D) data;
  }
}
