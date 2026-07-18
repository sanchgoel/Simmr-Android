package com.example.simmr.feature.onboarding.data

import com.example.simmr.feature.onboarding.model.OnboardingOption
import com.example.simmr.feature.onboarding.model.OnboardingOptionGroup
import com.example.simmr.feature.onboarding.model.OnboardingQuestion
import com.example.simmr.feature.onboarding.model.OnboardingQuestionKind.MultiSelect
import com.example.simmr.feature.onboarding.model.OnboardingQuestionKind.Ranking
import com.example.simmr.feature.onboarding.model.OnboardingQuestionKind.SingleSelect
import com.example.simmr.feature.onboarding.model.question

object OnboardingQuestions {
    val all: List<OnboardingQuestion> = listOf(
        question(
            id = "cook_frequency",
            sectionTitle = "About You",
            text = "How often do you cook?",
            kind = SingleSelect,
            options = listOf(
                o("daily", "Every day"),
                o("4_6_week", "4–6 times a week"),
                o("2_3_week", "2–3 times a week"),
                o("weekends", "Weekends"),
                o("once_week", "Once a week"),
                o("rarely", "Rarely"),
                o("just_starting", "I'm just starting to cook"),
            ),
        ),
        question(
            id = "cook_for",
            sectionTitle = "About You",
            text = "Who do you usually cook for?",
            kind = MultiSelect(),
            options = listOf(
                o("me", "Me"), o("partner", "Partner"), o("kids", "Kids"),
                o("parents", "Parents"), o("friends", "Friends"),
            ),
        ),
        question(
            id = "confidence",
            sectionTitle = "Your Cooking Style",
            text = "How confident are you in the kitchen?",
            kind = SingleSelect,
            options = listOf(
                o("never_cooked", "Never cooked before"), o("beginner", "Beginner"),
                o("comfortable", "Comfortable"), o("experienced", "Experienced"),
                o("experimenter", "I love experimenting"),
            ),
        ),
        question(
            id = "why_cook",
            sectionTitle = "Your Cooking Style",
            text = "Why do you cook?",
            kind = MultiSelect(),
            options = listOf(
                o("eat_healthier", "Eat healthier"), o("enjoy_cooking", "Enjoy cooking"),
                o("family_responsibility", "Family responsibility"), o("meal_prep", "Meal prep"),
                o("fitness", "Fitness"), o("relaxation", "Relaxation"),
            ),
        ),
        question(
            id = "frustration",
            sectionTitle = "Your Cooking Style",
            text = "What's your biggest cooking frustration?",
            kind = MultiSelect(maxSelections = 3),
            options = listOf(
                o("dont_know_what", "I never know what to cook"),
                o("confusing_recipes", "Recipes are confusing"),
                o("takes_too_long", "Cooking takes too long"),
                o("wasting_ingredients", "Wasting ingredients"),
                o("healthy_boring", "Healthy food is boring"),
                o("forget_recipes", "I forget recipes"),
            ),
        ),
        question(
            id = "appliances",
            sectionTitle = "Your Kitchen",
            text = "Which appliances do you have?",
            kind = MultiSelect(),
            options = listOf(
                o("gas_stove", "Gas stove"), o("induction", "Induction"),
                o("microwave", "Microwave"), o("oven", "Oven"), o("otg", "OTG"),
                o("air_fryer", "Air fryer"), o("pressure_cooker", "Pressure cooker"),
                o("instant_pot", "Instant Pot"), o("mixer_blender", "Mixer / Blender"),
                o("food_processor", "Food processor"),
            ),
        ),
        question(
            id = "time_available",
            sectionTitle = "Your Kitchen",
            text = "How much time do you usually have to cook?",
            kind = SingleSelect,
            options = listOf(
                o("under_15", "Under 15 mins"), o("15_30", "15–30 mins"),
                o("30_45", "30–45 mins"), o("45_60", "45–60 mins"),
                o("over_60", "More than an hour"),
            ),
        ),
        question(
            id = "meals_cooked",
            sectionTitle = "Your Kitchen",
            text = "Which meals do you usually cook?",
            kind = MultiSelect(),
            options = listOf(
                o("breakfast", "Breakfast"), o("lunch", "Lunch"), o("dinner", "Dinner"),
                o("snacks", "Snacks"), o("desserts", "Desserts"), o("drinks", "Drinks"),
                o("meal_prep_meals", "Meal prep"),
            ),
        ),
        question(
            id = "diet",
            sectionTitle = "Food Preferences",
            text = "Which best describes your diet?",
            kind = MultiSelect(),
            options = listOf(
                o("vegetarian", "Vegetarian"), o("eggetarian", "Eggetarian"),
                o("vegan", "Vegan"), o("chicken", "Chicken"), o("fish", "Fish"),
                o("seafood", "Seafood"), o("beef", "Beef"), o("pork", "Pork"),
                o("no_preference", "No preference", exclusive = true),
            ),
        ),
        question(
            id = "restrictions",
            sectionTitle = "Dietary Restrictions & Health",
            text = "Do any of these apply to you?",
            kind = MultiSelect(),
            options = listOf(
                o("has_allergies", "🚫 I have food allergies"),
                o("has_medical", "🩺 I have a medical condition that affects what I eat"),
                o("avoids_by_choice", "🌱 I avoid certain foods by choice"),
                o("none_restrictions", "✅ None of these", exclusive = true),
            ),
        ),
        OnboardingQuestion(
            id = "allergies",
            sectionTitle = "Dietary Restrictions & Health",
            text = "Food Allergies",
            kind = MultiSelect(),
            groups = listOf(
                g("nuts", "Nuts", o("peanuts", "Peanuts"), o("almonds", "Almonds"),
                    o("cashews", "Cashews"), o("walnuts", "Walnuts"),
                    o("pistachios", "Pistachios"), o("other_tree_nuts", "Other tree nuts")),
                g("dairy_eggs", "Dairy & Eggs", o("milk", "Milk"), o("cheese", "Cheese"),
                    o("butter", "Butter"), o("eggs", "Eggs")),
                g("grains", "Grains", o("wheat", "Wheat"), o("gluten", "Gluten")),
                g("soy_legumes", "Soy & Legumes", o("soy", "Soy")),
                g("seafood_allergy", "Seafood", o("allergy_fish", "Fish"), o("shellfish", "Shellfish")),
                g("seeds_others", "Seeds & Others", o("sesame", "Sesame"),
                    o("mustard", "Mustard"), o("corn", "Corn")),
            ),
            allowsOtherText = true,
            isVisible = { it["restrictions"]?.selectedOptionIds?.contains("has_allergies") == true },
        ),
        OnboardingQuestion(
            id = "medical_conditions",
            sectionTitle = "Dietary Restrictions & Health",
            text = "Medical Conditions",
            subtitle = "We'll use this only to recommend recipes that better suit your dietary needs. This isn't medical advice.",
            kind = MultiSelect(),
            groups = listOf(
                g("blood_sugar", "Blood Sugar", o("diabetes", "Diabetes"), o("prediabetes", "Prediabetes")),
                g("heart_health", "Heart Health", o("high_blood_pressure", "High blood pressure"),
                    o("high_cholesterol", "High cholesterol"), o("heart_disease", "Heart disease")),
                g("digestive_health", "Digestive Health", o("ibs", "IBS"),
                    o("acid_reflux", "Acid reflux (GERD)"), o("lactose_intolerance", "Lactose intolerance"),
                    o("celiac_disease", "Celiac disease"), o("gluten_sensitivity", "Gluten sensitivity")),
                g("hormonal_metabolic", "Hormonal & Metabolic", o("pcos_pcod", "PCOS / PCOD"),
                    o("thyroid_condition", "Thyroid condition")),
                g("kidney_liver", "Kidney & Liver", o("kidney_disease", "Kidney disease"),
                    o("fatty_liver", "Fatty liver")),
                g("life_stage", "Life Stage", o("pregnancy", "Pregnancy"), o("breastfeeding", "Breastfeeding")),
            ),
            allowsOtherText = true,
            isVisible = { it["restrictions"]?.selectedOptionIds?.contains("has_medical") == true },
        ),
        question(
            id = "foods_avoided",
            sectionTitle = "Dietary Restrictions & Health",
            text = "Foods You Avoid",
            kind = MultiSelect(),
            options = listOf(
                o("avoid_beef", "Beef"), o("avoid_pork", "Pork"), o("avoid_seafood", "Seafood"),
                o("avoid_eggs", "Eggs"), o("avoid_dairy", "Dairy"), o("avoid_onion", "Onion"),
                o("avoid_garlic", "Garlic"), o("avoid_mushrooms", "Mushrooms"),
                o("avoid_alcohol", "Alcohol"), o("avoid_caffeine", "Caffeine"),
                o("avoid_added_sugar", "Added sugar"),
            ),
            allowsOtherText = true,
            isVisible = { it["restrictions"]?.selectedOptionIds?.contains("avoids_by_choice") == true },
        ),
        question(
            id = "nutrition_goals",
            sectionTitle = "Health Goals",
            text = "What are your current nutrition goals?",
            kind = MultiSelect(),
            options = listOf(
                o("lose_weight", "Lose weight"), o("build_muscle", "Build muscle"),
                o("maintain_weight", "Maintain weight"), o("more_protein", "Eat more protein"),
                o("more_vegetables", "Eat more vegetables"), o("improve_digestion", "Improve digestion"),
                o("reduce_sugar", "Reduce sugar"), o("increase_fibre", "Increase fibre"),
            ),
            allowsOtherText = true,
        ),
        question(
            id = "cuisines",
            sectionTitle = "Taste Preferences",
            text = "Which cuisines do you enjoy the most?",
            kind = MultiSelect(maxSelections = 5),
            options = listOf(
                o("indian", "Indian"), o("italian", "Italian"), o("chinese", "Chinese"),
                o("thai", "Thai"), o("japanese", "Japanese"), o("korean", "Korean"),
                o("mexican", "Mexican"), o("mediterranean", "Mediterranean"),
                o("middle_eastern", "Middle Eastern"), o("american", "American"),
            ),
            allowsOtherText = true,
        ),
        question(
            id = "spice_level",
            sectionTitle = "Taste Preferences",
            text = "How spicy do you like your food?",
            kind = SingleSelect,
            options = listOf(
                o("mild", "Mild"), o("medium", "Medium"),
                o("spicy", "Spicy"), o("very_spicy", "Very spicy"),
            ),
        ),
        question(
            id = "ai_help",
            sectionTitle = "AI Personalization",
            text = "What would you like your AI cooking companion to help you with?",
            subtitle = "Rank your top 3",
            kind = Ranking(count = 3),
            options = listOf(
                o("decide_what_to_cook", "Decide what to cook"),
                o("step_by_step_cooking", "Step-by-step cooking"),
                o("ingredient_substitutions", "Ingredient substitutions"),
                o("explain_techniques", "Explain cooking techniques"),
                o("grocery_lists", "Grocery lists"), o("meal_planning", "Meal planning"),
                o("healthier_alternatives", "Healthier alternatives"),
                o("nutrition_insights", "Nutrition insights"),
                o("portion_sizing", "Portion sizing"), o("leftover_ideas", "Leftover ideas"),
            ),
        ),
        question(
            id = "measurement_units",
            sectionTitle = "AI Personalization",
            text = "How would you like measurements to be shown?",
            kind = SingleSelect,
            options = listOf(
                o("metric", "Metric"), o("us_cups", "US Cups"), o("both_units", "Both"),
            ),
        ),
    )
}

private fun o(id: String, label: String, exclusive: Boolean = false) =
    OnboardingOption(id = id, label = label, isExclusive = exclusive)

private fun g(id: String, title: String, vararg options: OnboardingOption) =
    OnboardingOptionGroup(id = id, title = title, options = options.toList())
