package de.thi.mynd.topic.service;

import de.thi.mynd.common.dto.PaginationDto;
import de.thi.mynd.topic.dto.ListTopicDto;
import de.thi.mynd.topic.entity.Topic;
import de.thi.mynd.topic.repository.TopicRepository;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.security.Principal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;

@QuarkusTest
public class TopicServiceImplTest {

    @Inject
    TopicServiceImpl topicService;

    @InjectMock
    TopicRepository topicRepository;

    @InjectMock
    SecurityIdentity securityIdentity;

    /**
     * Helper to mock the security context for a specific username
     */
    private void mockUser(String username) {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        Mockito.when(mockPrincipal.getName()).thenReturn(username);
        Mockito.when(securityIdentity.getPrincipal()).thenReturn(mockPrincipal);
    }

    @Test
    public void testFindPersonalTopicsForDifferentUser() {
        String alice = "alice-123";
        String bob = "bob-456";
        mockUser(bob);

        Topic bobsTopic = new Topic();
        bobsTopic.title = "Bob's Private Topic";

        PaginationDto<Topic> bobsData = PaginationDto.<Topic>builder()
                .results(List.of(bobsTopic))
                .totalPages(1)
                .build();

        Mockito.when(topicRepository.findForCreatorPaginated(eq(bob), anyInt(), anyInt()))
                .thenReturn(bobsData);

        Mockito.when(topicRepository.findForCreatorPaginated(eq(alice), anyInt(), anyInt()))
                .thenReturn(PaginationDto.<Topic>builder().results(List.of()).build());

        PaginationDto<ListTopicDto> result = topicService.findPersonalTopicsPaginated(0, 10);

        Assertions.assertEquals("Bob's Private Topic", result.results.get(0).title);
        Mockito.verify(topicRepository).findForCreatorPaginated(bob, 0, 10);
        Mockito.verify(topicRepository, Mockito.never()).findForCreatorPaginated(eq(alice), anyInt(), anyInt());
    }

    @Test
    public void testEmptyResults() {
        mockUser("lonely-user");

        Mockito.when(topicRepository.findForCreatorPaginated(anyString(), anyInt(), anyInt()))
                .thenReturn(PaginationDto.<Topic>builder()
                        .results(List.of())
                        .totalPages(0)
                        .build());

        PaginationDto<ListTopicDto> result = topicService.findPersonalTopicsPaginated(0, 10);

        Assertions.assertTrue(result.results.isEmpty());
        Assertions.assertEquals(0, result.totalPages);
    }

    @Test
    public void testUnauthorizedIfPrincipalMissing() {
        Mockito.when(securityIdentity.getPrincipal()).thenReturn(null);

        Assertions.assertThrows(NullPointerException.class, () -> {
            topicService.findPersonalTopicsPaginated(0, 10);
        });
    }
}