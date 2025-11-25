package tqs.sportslink.boundary;



@RestController
@RequestMapping("/api/onwer")
@PreAuthorize("hasRole('OWNER')")
public class OwnerController {
    
}
