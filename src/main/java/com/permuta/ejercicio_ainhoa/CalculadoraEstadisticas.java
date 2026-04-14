package com.permuta.ejercicio_ainhoa;

import java.util.Collections;
import java.util.List;

public class CalculadoraEstadisticas {

    public static int getCuentaCantidadNumeros(List<Integer> numeros) {
        return numeros.size();
    }

    public static int getSumaNumeros(List<Integer> numeros){
        return numeros.stream()
                .reduce(0, (acumulado, n) -> acumulado + n);
    }

    public static int getMinimo(List<Integer> numeros){
        return numeros.stream()
                .min(Integer::compare)
              //.min((n1, n2) -> Integer.compare(n1,n2)) es lo mismo
                .orElse(0);
    }


    public static int getMaximo(List<Integer> numeros){
        return numeros.stream()
                .max(Integer::compare)
                .orElse(0);
    }

    public static double getMedia(List<Integer> numeros){
        int cantidadNumeros = getCuentaCantidadNumeros(numeros);

        if(cantidadNumeros == 0){
            return Integer.MAX_VALUE;
        }
        return (double) getSumaNumeros(numeros) / cantidadNumeros;
    }

    public static double getMediana(List<Integer> numeros){

        if(numeros.isEmpty()){
            return Integer.MAX_VALUE;
        }

        List<Integer> sorted = numeros.stream().sorted().toList();
        int mediana = sorted.size() / 2;
        //en programación entero entre entero da entero aunque de decimal, 7/3 = 3 aunque sea 3,5

        if(sorted.size() % 2 == 1){
            return sorted.get(mediana);
        }else{
            return (sorted.get(mediana) +  sorted.get(mediana + 1)) / 2.0;
        }
    }


    public static int getModa(List<Integer> numeros){

        if(numeros.isEmpty()){
            return Integer.MAX_VALUE;
        }

        return numeros.stream()
                .reduce((actual, n) ->
                        Collections.frequency(numeros, n) > Collections.frequency(numeros, actual) ? n : actual)
                //si la frecuencia en la que aparece el siguiente n es mayor que la frecuencia de mayor numero de frecuencia actual
                .orElse(Integer.MAX_VALUE);
    }
}
