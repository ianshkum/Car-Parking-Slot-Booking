package com.Park.services;

import com.Park.entities.Rating;
import com.Park.repositories.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RatingServiceImpl implements RatingService {

    @Autowired
    private RatingRepository ratingRepository;


    @Override
    public Rating save(Rating rating) {
        return ratingRepository.saveAndFlush(rating);
    }


}
