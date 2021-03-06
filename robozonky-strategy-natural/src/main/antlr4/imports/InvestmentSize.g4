grammar InvestmentSize;

import Tokens;

@header {
    import java.util.Map;
    import java.util.EnumMap;
    import com.github.robozonky.strategy.natural.*;
}

investmentSizeExpression returns [Map<Rating, MoneyRange> result]:
    {
        final EnumMap<Rating, MoneyRange> result = new EnumMap<>(Rating.class);
    }
    (i=investmentSizeInterestRateExpression { result.put($i.rating, $i.size); })+ {
        $result = result;
    }
;

investmentSizeInterestRateExpression returns [Rating rating, MoneyRange size] :
    'Do půjček s úročením ' r=interestRateBasedRatingExpression 'investovat po ' i=intExpr KC DOT {
        $rating = $r.result;
        $size = new MoneyRange($i.result, $i.result);
    }
;

purchaseSizeExpression returns [Map<Rating, MoneyRange> result]:
    {
        final EnumMap<Rating, MoneyRange> result = new EnumMap<>(Rating.class);
    }
    (i=purchaseSizeInterestRateExpression { result.put($i.rating, $i.size); })+ {
        $result = result;
    }
;

purchaseSizeInterestRateExpression returns [Rating rating, MoneyRange size] :
    'Participace s úročením ' r=interestRateBasedRatingExpression 'nakupovat nejvýše za ' i=intExpr KC DOT {
        $rating = $r.result;
        $size = new MoneyRange(1, $i.result);
    }
;
